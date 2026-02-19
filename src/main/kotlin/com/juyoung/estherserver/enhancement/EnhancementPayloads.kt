package com.juyoung.estherserver.enhancement

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class EnhanceItemPayload(val profession: String, val action: String) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnhanceItemPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enhance_item")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnhanceItemPayload> =
            object : StreamCodec<FriendlyByteBuf, EnhanceItemPayload> {
                override fun decode(buf: FriendlyByteBuf): EnhanceItemPayload {
                    return EnhanceItemPayload(buf.readUtf(), buf.readUtf())
                }

                override fun encode(buf: FriendlyByteBuf, value: EnhanceItemPayload) {
                    buf.writeUtf(value.profession)
                    buf.writeUtf(value.action)
                }
            }
    }
}
