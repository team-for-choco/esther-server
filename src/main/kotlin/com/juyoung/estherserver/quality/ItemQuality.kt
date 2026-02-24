package com.juyoung.estherserver.quality

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.ChatFormatting
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.RandomSource
import net.minecraft.util.StringRepresentable

enum class ItemQuality(
    private val serializedName: String,
    val color: ChatFormatting,
    val weight: Int,
    val translationKey: String
) : StringRepresentable {
    COMMON("common", ChatFormatting.WHITE, 70, "quality.estherserver.common"),
    FINE("fine", ChatFormatting.GREEN, 25, "quality.estherserver.fine"),
    RARE("rare", ChatFormatting.BLUE, 5, "quality.estherserver.rare");

    override fun getSerializedName(): String = serializedName

    companion object {
        val CODEC: Codec<ItemQuality> = StringRepresentable.fromEnum(::values)
        val STREAM_CODEC: StreamCodec<ByteBuf, ItemQuality> = ByteBufCodecs.idMapper(
            { entries[it] }, { it.ordinal }
        )

        private val BY_NAME: Map<String, ItemQuality> = entries.associateBy { it.getSerializedName() }
        private val TOTAL_WEIGHT = entries.sumOf { it.weight }

        fun byName(name: String): ItemQuality? = BY_NAME[name]

        fun randomQuality(random: RandomSource): ItemQuality {
            var roll = random.nextInt(TOTAL_WEIGHT)
            for (quality in entries) {
                roll -= quality.weight
                if (roll < 0) return quality
            }
            return COMMON
        }

        fun randomQualityWithBonus(random: RandomSource, fineBonus: Int, rareBonus: Int): ItemQuality {
            if (fineBonus == 0 && rareBonus == 0) return randomQuality(random)
            val commonWeight = maxOf(10, COMMON.weight - fineBonus - rareBonus)
            val fineWeight = FINE.weight + fineBonus
            val rareWeight = RARE.weight + rareBonus
            val total = commonWeight + fineWeight + rareWeight
            var roll = random.nextInt(total)
            roll -= commonWeight
            if (roll < 0) return COMMON
            roll -= fineWeight
            if (roll < 0) return FINE
            return RARE
        }
    }
}
