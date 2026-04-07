package com.juyoung.estherserver.enchant

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class OpenEnchantMerchantPayload : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<OpenEnchantMerchantPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "open_enchant_merchant")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, OpenEnchantMerchantPayload> =
            object : StreamCodec<FriendlyByteBuf, OpenEnchantMerchantPayload> {
                override fun decode(buf: FriendlyByteBuf) = OpenEnchantMerchantPayload()
                override fun encode(buf: FriendlyByteBuf, value: OpenEnchantMerchantPayload) {}
            }
    }
}

class EnchantRequestPayload(val mode: String) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantRequestPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_request")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantRequestPayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantRequestPayload> {
                override fun decode(buf: FriendlyByteBuf) = EnchantRequestPayload(buf.readUtf())
                override fun encode(buf: FriendlyByteBuf, value: EnchantRequestPayload) {
                    buf.writeUtf(value.mode)
                }
            }
    }
}

class EnchantPreviewPayload(val enchantId: String, val level: Int) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantPreviewPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_preview")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantPreviewPayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantPreviewPayload> {
                override fun decode(buf: FriendlyByteBuf) = EnchantPreviewPayload(buf.readUtf(), buf.readVarInt())
                override fun encode(buf: FriendlyByteBuf, value: EnchantPreviewPayload) {
                    buf.writeUtf(value.enchantId)
                    buf.writeVarInt(value.level)
                }
            }
    }
}

class EnchantConfirmPayload(val accept: Boolean) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantConfirmPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_confirm")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantConfirmPayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantConfirmPayload> {
                override fun decode(buf: FriendlyByteBuf) = EnchantConfirmPayload(buf.readBoolean())
                override fun encode(buf: FriendlyByteBuf, value: EnchantConfirmPayload) {
                    buf.writeBoolean(value.accept)
                }
            }
    }
}
