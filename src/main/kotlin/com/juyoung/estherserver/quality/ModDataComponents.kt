package com.juyoung.estherserver.quality

import com.juyoung.estherserver.EstherServerMod
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import java.util.function.Supplier

object ModDataComponents {
    val DATA_COMPONENTS: DeferredRegister<DataComponentType<*>> =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EstherServerMod.MODID)

    val ITEM_QUALITY: DeferredHolder<DataComponentType<*>, DataComponentType<ItemQuality>> =
        DATA_COMPONENTS.register("item_quality", Supplier {
            DataComponentType.builder<ItemQuality>()
                .persistent(ItemQuality.CODEC)
                .networkSynchronized(ItemQuality.STREAM_CODEC)
                .build()
        })
}
