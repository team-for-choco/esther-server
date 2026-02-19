package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionHandler
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.registries.Registries
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

        for (stack in generatedLoot) {
            if (stack.`is`(HAS_QUALITY_TAG)) {
                val quality = ItemQuality.randomQuality(context.random)
                stack.set(ModDataComponents.ITEM_QUALITY.get(), quality)

                // Grant profession XP only with correct special tool
                if (player != null) {
                    val profession = ProfessionHandler.getProfessionForItem(stack)
                    if (profession != null && isCorrectToolForProfession(tool, profession)) {
                        val xp = ProfessionHandler.getXpForQuality(quality)
                        ProfessionHandler.addExperience(player, profession, xp)
                        relevantProfessions.add(profession)
                    }
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
                    generatedLoot.add(ItemStack(EstherServerMod.ENHANCEMENT_STONE.get()))
                    break
                }
            }
        }

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
