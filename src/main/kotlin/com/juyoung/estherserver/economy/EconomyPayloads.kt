package com.juyoung.estherserver.economy

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class BalanceSyncPayload(val balance: Long) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<BalanceSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "balance_sync")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, BalanceSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, BalanceSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): BalanceSyncPayload {
                    return BalanceSyncPayload(buf.readLong())
                }

                override fun encode(buf: FriendlyByteBuf, value: BalanceSyncPayload) {
                    buf.writeLong(value.balance)
                }
            }
    }
}
