package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.profession.Profession
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

class EnhancementPityData(
    private val pityMap: MutableMap<Profession, Double> = mutableMapOf()
) {
    fun getPity(profession: Profession): Double = pityMap.getOrDefault(profession, 0.0)

    fun addPity(profession: Profession, amount: Double) {
        val current = getPity(profession)
        pityMap[profession] = (current + amount).coerceAtMost(1.0)
    }

    fun resetPity(profession: Profession) {
        pityMap.remove(profession)
    }

    fun isGuaranteed(profession: Profession): Boolean = getPity(profession) >= 1.0

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        for ((profession, value) in pityMap) {
            tag.putDouble(profession.name, value)
        }
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): EnhancementPityData {
            val map = mutableMapOf<Profession, Double>()
            for (profession in Profession.entries) {
                if (tag.contains(profession.name)) {
                    map[profession] = tag.getDouble(profession.name)
                }
            }
            return EnhancementPityData(map)
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnhancementPityData> =
            object : StreamCodec<FriendlyByteBuf, EnhancementPityData> {
                override fun decode(buf: FriendlyByteBuf): EnhancementPityData {
                    val map = mutableMapOf<Profession, Double>()
                    val size = buf.readInt()
                    repeat(size) {
                        val profession = Profession.entries[buf.readInt()]
                        val value = buf.readDouble()
                        map[profession] = value
                    }
                    return EnhancementPityData(map)
                }

                override fun encode(buf: FriendlyByteBuf, value: EnhancementPityData) {
                    buf.writeInt(value.pityMap.size)
                    for ((profession, pity) in value.pityMap) {
                        buf.writeInt(profession.ordinal)
                        buf.writeDouble(pity)
                    }
                }
            }
    }
}
