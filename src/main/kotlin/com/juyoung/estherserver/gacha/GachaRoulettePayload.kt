package com.juyoung.estherserver.gacha

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class RouletteDisplayEntry(
    val itemId: String,
    val count: Int,
    val displayKey: String,
    val isCurrency: Boolean,
    val currencyAmount: Long
)

class GachaRoulettePayload(
    val entries: List<RouletteDisplayEntry>,
    val winnerIndex: Int,
    val poolId: String
) : CustomPacketPayload {

    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<GachaRoulettePayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "gacha_roulette")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, GachaRoulettePayload> =
            object : StreamCodec<FriendlyByteBuf, GachaRoulettePayload> {
                override fun decode(buf: FriendlyByteBuf): GachaRoulettePayload {
                    val count = buf.readVarInt()
                    val entries = mutableListOf<RouletteDisplayEntry>()
                    repeat(count) {
                        entries.add(
                            RouletteDisplayEntry(
                                itemId = buf.readUtf(),
                                count = buf.readVarInt(),
                                displayKey = buf.readUtf(),
                                isCurrency = buf.readBoolean(),
                                currencyAmount = buf.readLong()
                            )
                        )
                    }
                    val winnerIndex = buf.readVarInt()
                    val poolId = buf.readUtf()
                    return GachaRoulettePayload(entries, winnerIndex, poolId)
                }

                override fun encode(buf: FriendlyByteBuf, value: GachaRoulettePayload) {
                    buf.writeVarInt(value.entries.size)
                    for (entry in value.entries) {
                        buf.writeUtf(entry.itemId)
                        buf.writeVarInt(entry.count)
                        buf.writeUtf(entry.displayKey)
                        buf.writeBoolean(entry.isCurrency)
                        buf.writeLong(entry.currencyAmount)
                    }
                    buf.writeVarInt(value.winnerIndex)
                    buf.writeUtf(value.poolId)
                }
            }
    }
}
