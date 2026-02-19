package com.juyoung.estherserver.quality

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
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

    val ENHANCEMENT_LEVEL: DeferredHolder<DataComponentType<*>, DataComponentType<Int>> =
        DATA_COMPONENTS.register("enhancement_level", Supplier {
            DataComponentType.builder<Int>()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build()
        })

    val WATER_CHARGES: DeferredHolder<DataComponentType<*>, DataComponentType<Int>> =
        DATA_COMPONENTS.register("water_charges", Supplier {
            DataComponentType.builder<Int>()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT)
                .build()
        })
}
