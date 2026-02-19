package com.juyoung.estherserver.profession

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

class ProfessionData(
    private val xp: MutableMap<Profession, Int> = mutableMapOf(),
    private val levels: MutableMap<Profession, Int> = mutableMapOf()
) {
    fun getXp(profession: Profession): Int = xp.getOrDefault(profession, 0)

    fun getLevel(profession: Profession): Int = levels.getOrDefault(profession, 0)

    fun setXp(profession: Profession, value: Int) {
        xp[profession] = value
    }

    fun setLevel(profession: Profession, value: Int) {
        levels[profession] = value
    }

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        for (profession in Profession.entries) {
            val profTag = CompoundTag()
            profTag.putInt("xp", getXp(profession))
            profTag.putInt("level", getLevel(profession))
            tag.put(profession.name, profTag)
        }
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): ProfessionData {
            val data = ProfessionData()
            for (profession in Profession.entries) {
                if (tag.contains(profession.name)) {
                    val profTag = tag.getCompound(profession.name)
                    data.setXp(profession, profTag.getInt("xp"))
                    data.setLevel(profession, profTag.getInt("level"))
                }
            }
            return data
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, ProfessionData> = object : StreamCodec<FriendlyByteBuf, ProfessionData> {
            override fun decode(buf: FriendlyByteBuf): ProfessionData {
                val data = ProfessionData()
                val count = buf.readInt()
                repeat(count) {
                    val ordinal = buf.readInt()
                    val xp = buf.readInt()
                    val level = buf.readInt()
                    if (ordinal in Profession.entries.indices) {
                        val profession = Profession.entries[ordinal]
                        data.setXp(profession, xp)
                        data.setLevel(profession, level)
                    }
                }
                return data
            }

            override fun encode(buf: FriendlyByteBuf, value: ProfessionData) {
                buf.writeInt(Profession.entries.size)
                for (profession in Profession.entries) {
                    buf.writeInt(profession.ordinal)
                    buf.writeInt(value.getXp(profession))
                    buf.writeInt(value.getLevel(profession))
                }
            }
        }
    }
}
