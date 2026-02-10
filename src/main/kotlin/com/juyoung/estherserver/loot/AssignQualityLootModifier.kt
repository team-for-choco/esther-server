package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class AssignQualityLootModifier(
    conditions: Array<LootItemCondition>
) : LootModifier(conditions) {

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        for (stack in generatedLoot) {
            if (stack.`is`(HAS_QUALITY_TAG)) {
                val quality = ItemQuality.randomQuality(context.random)
                stack.set(ModDataComponents.ITEM_QUALITY.get(), quality)
            }
        }
        return generatedLoot
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
