package com.juyoung.estherserver.merchant

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class OpenShopPayload(val entityId: Int, val merchantType: String) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<OpenShopPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "open_shop")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, OpenShopPayload> =
            object : StreamCodec<FriendlyByteBuf, OpenShopPayload> {
                override fun decode(buf: FriendlyByteBuf): OpenShopPayload {
                    return OpenShopPayload(buf.readVarInt(), buf.readUtf())
                }

                override fun encode(buf: FriendlyByteBuf, value: OpenShopPayload) {
                    buf.writeVarInt(value.entityId)
                    buf.writeUtf(value.merchantType)
                }
            }
    }
}

class BuyItemPayload(val itemId: String, val quantity: Int) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<BuyItemPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "buy_item")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, BuyItemPayload> =
            object : StreamCodec<FriendlyByteBuf, BuyItemPayload> {
                override fun decode(buf: FriendlyByteBuf): BuyItemPayload {
                    return BuyItemPayload(buf.readUtf(), buf.readVarInt())
                }

                override fun encode(buf: FriendlyByteBuf, value: BuyItemPayload) {
                    buf.writeUtf(value.itemId)
                    buf.writeVarInt(value.quantity)
                }
            }
    }
}

class SellItemPayload(val entityId: Int, val slotIndex: Int, val quantity: Int) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<SellItemPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "sell_item")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, SellItemPayload> =
            object : StreamCodec<FriendlyByteBuf, SellItemPayload> {
                override fun decode(buf: FriendlyByteBuf): SellItemPayload {
                    return SellItemPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
                }

                override fun encode(buf: FriendlyByteBuf, value: SellItemPayload) {
                    buf.writeVarInt(value.entityId)
                    buf.writeVarInt(value.slotIndex)
                    buf.writeVarInt(value.quantity)
                }
            }
    }
}
