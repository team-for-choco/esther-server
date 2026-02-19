package com.juyoung.estherserver.economy

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

class BalanceData(
    var balance: Long = 0L
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putLong("balance", balance)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): BalanceData {
            return BalanceData(
                balance = tag.getLong("balance")
            )
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, BalanceData> = object : StreamCodec<FriendlyByteBuf, BalanceData> {
            override fun decode(buf: FriendlyByteBuf): BalanceData {
                return BalanceData(balance = buf.readLong())
            }

            override fun encode(buf: FriendlyByteBuf, value: BalanceData) {
                buf.writeLong(value.balance)
            }
        }
    }
}
