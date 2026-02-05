package com.juyoung.estherserver.loot

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class AddItemLootModifier(
    conditions: Array<LootItemCondition>,
    private val item: Item
) : LootModifier(conditions) {

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        generatedLoot.add(ItemStack(item))
        return generatedLoot
    }

    override fun codec(): MapCodec<out IGlobalLootModifier> = CODEC

    companion object {
        val CODEC: MapCodec<AddItemLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).and(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter { it.item }
            ).apply(inst, ::AddItemLootModifier)
        }
    }
}
