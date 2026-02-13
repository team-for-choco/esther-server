package com.juyoung.estherserver.sitting

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class SitPayload : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<SitPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "sit")
        )

        val STREAM_CODEC: StreamCodec<ByteBuf, SitPayload> = object : StreamCodec<ByteBuf, SitPayload> {
            override fun decode(buf: ByteBuf): SitPayload = SitPayload()
            override fun encode(buf: ByteBuf, value: SitPayload) {}
        }
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
