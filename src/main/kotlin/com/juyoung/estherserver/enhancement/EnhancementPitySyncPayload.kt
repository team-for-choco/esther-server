package com.juyoung.estherserver.enhancement

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class EnhancementPitySyncPayload(val data: EnhancementPityData) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnhancementPitySyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enhancement_pity_sync")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnhancementPitySyncPayload> =
            object : StreamCodec<FriendlyByteBuf, EnhancementPitySyncPayload> {
                override fun decode(buf: FriendlyByteBuf): EnhancementPitySyncPayload {
                    return EnhancementPitySyncPayload(EnhancementPityData.STREAM_CODEC.decode(buf))
                }

                override fun encode(buf: FriendlyByteBuf, value: EnhancementPitySyncPayload) {
                    EnhancementPityData.STREAM_CODEC.encode(buf, value.data)
                }
            }
    }
}
