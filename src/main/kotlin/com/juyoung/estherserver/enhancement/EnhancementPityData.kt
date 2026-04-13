package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.profession.Profession
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

class EnhancementPityData(
    private val pityMap: MutableMap<Profession, Int> = mutableMapOf()
) {
    fun getPityPercent(profession: Profession): Int = pityMap.getOrDefault(profession, 0)

    fun getPity(profession: Profession): Double = getPityPercent(profession).toDouble() / 100.0

    fun addPity(profession: Profession, amount: Int) {
        val current = pityMap.getOrDefault(profession, 0)
        pityMap[profession] = (current + amount).coerceAtMost(100)
    }

    fun resetPity(profession: Profession) {
        pityMap.remove(profession)
    }

    fun isGuaranteed(profession: Profession): Boolean = getPityPercent(profession) >= 100

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        for ((profession, value) in pityMap) {
            tag.putInt(profession.name, value)
        }
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): EnhancementPityData {
            val map = mutableMapOf<Profession, Int>()
            for (profession in Profession.entries) {
                if (tag.contains(profession.name)) {
                    map[profession] = tag.getInt(profession.name)
                }
            }
            return EnhancementPityData(map)
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EnhancementPityData> =
            object : StreamCodec<FriendlyByteBuf, EnhancementPityData> {
                override fun decode(buf: FriendlyByteBuf): EnhancementPityData {
                    val map = mutableMapOf<Profession, Int>()
                    val size = buf.readInt()
                    repeat(size) {
                        val profession = buf.readEnum(Profession::class.java)
                        val value = buf.readInt()
                        map[profession] = value
                    }
                    return EnhancementPityData(map)
                }

                override fun encode(buf: FriendlyByteBuf, value: EnhancementPityData) {
                    buf.writeInt(value.pityMap.size)
                    for ((profession, pity) in value.pityMap) {
                        buf.writeEnum(profession)
                        buf.writeInt(pity)
                    }
                }
            }
    }
}
