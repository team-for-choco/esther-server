package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionHandler
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class AssignQualityLootModifier(
    conditions: Array<LootItemCondition>
) : LootModifier(conditions) {

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        val player = getPlayerFromContext(context)
        val tool = context.getOptionalParameter(LootContextParams.TOOL)

        val relevantProfessions = mutableSetOf<Profession>()
        val extraDrops = mutableListOf<ItemStack>()

        for (stack in generatedLoot) {
            if (stack.`is`(HAS_QUALITY_TAG)) {
                // Calculate quality bonuses from profession level
                var fineBonus = 0
                var rareBonus = 0
                val profession = ProfessionHandler.getProfessionForItem(stack)
                if (player != null && profession != null && isCorrectToolForProfession(tool, profession)) {
                    val profLevel = ProfessionHandler.getLevel(player, profession)
                    fineBonus = ProfessionBonusHelper.getFineQualityBonus(profLevel)
                    rareBonus = ProfessionBonusHelper.getRareQualityBonus(profLevel)
                }

                val quality = ItemQuality.randomQualityWithBonus(context.random, fineBonus, rareBonus)
                stack.set(ModDataComponents.ITEM_QUALITY.get(), quality)

                // Grant profession XP only with correct special tool
                if (player != null && profession != null && isCorrectToolForProfession(tool, profession)) {
                    val xp = ProfessionHandler.getXpForQuality(quality)
                    ProfessionHandler.addExperience(player, profession, xp)
                    relevantProfessions.add(profession)
                }
            }
        }

        // Vanilla mining XP (no quality, fixed XP per ore type)
        if (player != null && isCorrectToolForProfession(tool, Profession.MINING)) {
            for (stack in generatedLoot) {
                val xp = ProfessionHandler.getVanillaMiningXp(stack)
                if (xp != null) {
                    ProfessionHandler.addExperience(player, Profession.MINING, xp)
                    relevantProfessions.add(Profession.MINING)
                }
            }
        }

        // Enhancement stone drop (fishing/mining with Lv4+ equipment)
        if (player != null) {
            for (prof in relevantProfessions) {
                if (prof != Profession.FISHING && prof != Profession.MINING) continue
                val equipLevel = EnhancementHandler.getEquipmentLevel(player, prof)
                if (equipLevel >= 4 && context.random.nextFloat() < EnhancementHandler.ENHANCEMENT_STONE_DROP_RATE) {
                    extraDrops.add(ItemStack(EstherServerMod.ENHANCEMENT_STONE.get()))
                    break
                }
            }
        }

        // --- Lv50 & Equipment bonus effects (extra drops, no XP to prevent loops) ---
        if (player != null) {
            // Mining Lv50: 30% double drop
            if (Profession.MINING in relevantProfessions) {
                val profLevel = ProfessionHandler.getLevel(player, Profession.MINING)
                if (ProfessionBonusHelper.shouldDoubleMineDrop(profLevel, context.random)) {
                    for (stack in generatedLoot) {
                        if (stack.`is`(HAS_QUALITY_TAG) || ProfessionHandler.getVanillaMiningXp(stack) != null) {
                            extraDrops.add(stack.copy())
                        }
                    }
                    player.displayClientMessage(
                        Component.translatable("message.estherserver.double_mine_drop"), false
                    )
                }
            }

            // Fishing Lv50: 25% double fish
            if (Profession.FISHING in relevantProfessions) {
                val profLevel = ProfessionHandler.getLevel(player, Profession.FISHING)
                if (ProfessionBonusHelper.shouldDoubleFish(profLevel, context.random)) {
                    for (stack in generatedLoot) {
                        if (stack.`is`(HAS_QUALITY_TAG)) {
                            val bonusCopy = stack.copy()
                            val bonusQuality = ItemQuality.randomQuality(context.random)
                            bonusCopy.set(ModDataComponents.ITEM_QUALITY.get(), bonusQuality)
                            extraDrops.add(bonusCopy)
                        }
                    }
                    player.displayClientMessage(
                        Component.translatable("message.estherserver.double_fish"), false
                    )
                }
            }

            // Farming equipment Lv3+: extra harvest chance
            if (Profession.FARMING in relevantProfessions) {
                val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FARMING)
                val extraChance = ProfessionBonusHelper.getExtraHarvestChance(equipLevel)
                if (extraChance > 0f && context.random.nextFloat() < extraChance) {
                    for (stack in generatedLoot) {
                        if (stack.`is`(HAS_QUALITY_TAG) && ProfessionHandler.getProfessionForItem(stack) == Profession.FARMING) {
                            extraDrops.add(stack.copy())
                        }
                    }
                }

                // Farming Lv50: 30% seed preservation
                val profLevel = ProfessionHandler.getLevel(player, Profession.FARMING)
                if (ProfessionBonusHelper.shouldPreserveSeed(profLevel, context.random)) {
                    val blockState = context.getOptionalParameter(LootContextParams.BLOCK_STATE)
                    if (blockState != null) {
                        val blockId = BuiltInRegistries.BLOCK.getKey(blockState.block)
                        val seedStack = ProfessionHandler.getSeedForCrop(blockId)
                        if (seedStack != null) {
                            extraDrops.add(seedStack)
                            player.displayClientMessage(
                                Component.translatable("message.estherserver.seed_preserved"), false
                            )
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
        val HAS_QUALITY_TAG: TagKey<Item> = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "has_quality")
        )

        val CODEC: MapCodec<AssignQualityLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).apply(inst, ::AssignQualityLootModifier)
        }
    }
}
