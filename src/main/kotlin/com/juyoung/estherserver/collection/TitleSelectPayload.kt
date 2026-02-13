package com.juyoung.estherserver.collection

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class TitleSelectPayload(val milestoneId: String) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<TitleSelectPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "title_select")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, TitleSelectPayload> =
            object : StreamCodec<FriendlyByteBuf, TitleSelectPayload> {
                override fun decode(buf: FriendlyByteBuf): TitleSelectPayload {
                    return TitleSelectPayload(buf.readUtf())
                }

                override fun encode(buf: FriendlyByteBuf, value: TitleSelectPayload) {
                    buf.writeUtf(value.milestoneId)
                }
            }
    }
}
