package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionHandler
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class ProfessionLootModifier(
    conditions: Array<LootItemCondition>
) : LootModifier(conditions) {

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        val player = getPlayerFromContext(context)
        val tool = context.getOptionalParameter(LootContextParams.TOOL)

        val relevantProfessions = mutableSetOf<Profession>()
        val extraDrops = mutableListOf<ItemStack>()
        val toRemove = mutableListOf<ItemStack>()

        // --- Fish grade filtering: remove custom fish that exceed rod's enhancement level ---
        if (player != null && isCorrectToolForProfession(tool, Profession.FISHING)) {
            val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FISHING)
            for (stack in generatedLoot) {
                val fishGrade = ProfessionBonusHelper.getFishGrade(BuiltInRegistries.ITEM.getKey(stack.item))
                if (fishGrade != null) {
                    if (!ProfessionBonusHelper.canCatchCustomFish(equipLevel) ||
                        fishGrade > ProfessionBonusHelper.getMaxFishGrade(equipLevel)) {
                        toRemove.add(stack)
                    }
                }
            }
        }

        // --- Crop grade filtering: remove custom crops that exceed hoe's enhancement level ---
        if (player != null && isCorrectToolForProfession(tool, Profession.FARMING)) {
            val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FARMING)
            for (stack in generatedLoot) {
                val cropGrade = ProfessionBonusHelper.getCropGrade(BuiltInRegistries.ITEM.getKey(stack.item))
                if (cropGrade != null) {
                    if (!ProfessionBonusHelper.canHarvestCustomCrops(equipLevel) ||
                        cropGrade > ProfessionBonusHelper.getMaxCropGrade(equipLevel)) {
                        toRemove.add(stack)
                    }
                }
            }
        }

        generatedLoot.removeAll(toRemove.toSet())

        // --- XP for custom item drops (grade-based: common=1, advanced=3, rare=5) ---
        for (stack in generatedLoot) {
            val profession = ProfessionHandler.getProfessionForItem(stack)
            if (player != null && profession != null && isCorrectToolForProfession(tool, profession)) {
                val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
                ProfessionHandler.addExperience(player, profession, ProfessionBonusHelper.getGradeXp(itemId))
                relevantProfessions.add(profession)

                // Quest tracking
                val gradeStr = ProfessionBonusHelper.getContentGradeForItem(itemId)?.name
                val trackingType = when (profession) {
                    Profession.FISHING -> com.juyoung.estherserver.quest.QuestTrackingType.CATCH_FISH
                    Profession.FARMING -> com.juyoung.estherserver.quest.QuestTrackingType.HARVEST_CROP
                    Profession.MINING -> com.juyoung.estherserver.quest.QuestTrackingType.MINE_ORE
                    else -> null
                }
                if (trackingType != null) {
                    com.juyoung.estherserver.quest.QuestHandler.trackProgress(player, trackingType, 1, gradeStr)
                }
            }
        }

        // Vanilla mining XP (fixed XP per ore type)
        if (player != null && isCorrectToolForProfession(tool, Profession.MINING)) {
            for (stack in generatedLoot) {
                val xp = ProfessionHandler.getVanillaMiningXp(stack)
                if (xp != null) {
                    ProfessionHandler.addExperience(player, Profession.MINING, xp)
                    relevantProfessions.add(Profession.MINING)
                    // Quest tracking for vanilla ores (no grade filter)
                    com.juyoung.estherserver.quest.QuestHandler.trackProgress(
                        player, com.juyoung.estherserver.quest.QuestTrackingType.MINE_ORE, 1, null
                    )
                }
            }
        }

        // --- Equipment effects (no XP to prevent loops) ---
        if (player != null) {
            // Pickaxe Lv4+: enhancement stone drop based on ore grade
            if (Profession.MINING in relevantProfessions) {
                val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.MINING)
                if (equipLevel >= 4) {
                    for (stack in generatedLoot) {
                        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
                        val oreGrade = ProfessionBonusHelper.getOreGrade(itemId)
                        if (oreGrade != null && context.random.nextFloat() < oreGrade.enhancementStoneDropRate) {
                            extraDrops.add(ItemStack(EstherServerMod.ENHANCEMENT_STONE.get()))
                            break
                        }
                    }
                }
            }

            // Fishing rod Lv3: 5% double fish
            if (Profession.FISHING in relevantProfessions) {
                val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FISHING)
                if (equipLevel >= 3 && context.random.nextFloat() < 0.05f) {
                    var doubled = false
                    for (stack in generatedLoot) {
                        if (ProfessionHandler.getProfessionForItem(stack) == Profession.FISHING) {
                            extraDrops.add(stack.copy())
                            doubled = true
                        }
                    }
                    if (doubled) {
                        player.displayClientMessage(
                            Component.translatable("message.estherserver.double_fish"), false
                        )
                    }
                }

                // Fishing rod Lv4: 1% enhancement stone drop
                if (equipLevel >= 4 && context.random.nextFloat() < ProfessionBonusHelper.FISHING_ENHANCEMENT_STONE_DROP_RATE) {
                    extraDrops.add(ItemStack(EstherServerMod.ENHANCEMENT_STONE.get()))
                }
            }

            // Hoe Lv4: 5% extra harvest
            if (Profession.FARMING in relevantProfessions) {
                val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FARMING)
                if (equipLevel >= 4 && context.random.nextFloat() < 0.05f) {
                    for (stack in generatedLoot) {
                        if (ProfessionHandler.getProfessionForItem(stack) == Profession.FARMING) {
                            extraDrops.add(stack.copy())
                        }
                    }
                }
            }
        }

        generatedLoot.addAll(extraDrops)
        return generatedLoot
    }

    private fun isCorrectToolForProfession(tool: ItemStack?, profession: Profession): Boolean {
        if (tool == null || tool.isEmpty) return false
        val expectedItem = EnhancementHandler.EQUIPMENT_MAP[profession]?.get() ?: return false
        return tool.item === expectedItem
    }

    private fun getPlayerFromContext(context: LootContext): ServerPlayer? {
        val entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY)
        if (entity is ServerPlayer) return entity
        if (entity is FishingHook) return entity.playerOwner as? ServerPlayer
        return null
    }

    override fun codec(): MapCodec<out IGlobalLootModifier> = CODEC

    companion object {
        val CODEC: MapCodec<ProfessionLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).apply(inst, ::ProfessionLootModifier)
        }
    }
}
