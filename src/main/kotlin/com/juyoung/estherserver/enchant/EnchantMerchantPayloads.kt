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

enum class EnchantMode { OVERWRITE, CHOOSE, UNLOCK }

class EnchantRequestPayload(val mode: EnchantMode) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantRequestPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_request")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantRequestPayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantRequestPayload> {
                override fun decode(buf: FriendlyByteBuf) =
                    EnchantRequestPayload(buf.readEnum(EnchantMode::class.java))
                override fun encode(buf: FriendlyByteBuf, value: EnchantRequestPayload) {
                    buf.writeEnum(value.mode)
                }
            }
    }
}

class EnchantPreviewPayload(val enchants: List<Pair<String, Int>>) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantPreviewPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_preview")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantPreviewPayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantPreviewPayload> {
                override fun decode(buf: FriendlyByteBuf): EnchantPreviewPayload {
                    val size = buf.readVarInt()
                    val enchants = (0 until size).map { Pair(buf.readUtf(), buf.readVarInt()) }
                    return EnchantPreviewPayload(enchants)
                }

                override fun encode(buf: FriendlyByteBuf, value: EnchantPreviewPayload) {
                    buf.writeVarInt(value.enchants.size)
                    for ((id, level) in value.enchants) {
                        buf.writeUtf(id)
                        buf.writeVarInt(level)
                    }
                }
            }
    }
}

class EnchantDonePayload : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<EnchantDonePayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "enchant_done")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnchantDonePayload> =
            object : StreamCodec<FriendlyByteBuf, EnchantDonePayload> {
                override fun decode(buf: FriendlyByteBuf) = EnchantDonePayload()
                override fun encode(buf: FriendlyByteBuf, value: EnchantDonePayload) {}
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
