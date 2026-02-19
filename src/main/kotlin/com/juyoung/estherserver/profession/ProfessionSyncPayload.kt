package com.juyoung.estherserver.profession

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class ProfessionSyncPayload(val data: ProfessionData) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ProfessionSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "profession_sync")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, ProfessionSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, ProfessionSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): ProfessionSyncPayload {
                    return ProfessionSyncPayload(ProfessionData.STREAM_CODEC.decode(buf))
                }

                override fun encode(buf: FriendlyByteBuf, value: ProfessionSyncPayload) {
                    ProfessionData.STREAM_CODEC.encode(buf, value.data)
                }
            }
    }
}
