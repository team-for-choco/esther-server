package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object ModLootModifiers {
    val LOOT_MODIFIERS: DeferredRegister<MapCodec<out IGlobalLootModifier>> =
        DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EstherServerMod.MODID)

    val ADD_ITEM = LOOT_MODIFIERS.register("add_item", Supplier { AddItemLootModifier.CODEC })
}
