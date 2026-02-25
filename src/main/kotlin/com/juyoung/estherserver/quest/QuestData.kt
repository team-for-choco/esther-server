package com.juyoung.estherserver.quest

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

data class ActiveQuest(
    val templateId: String,
    var progress: Int = 0,
    var claimed: Boolean = false
) {
    fun isComplete(template: QuestTemplate): Boolean = progress >= template.targetCount

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putString("TemplateId", templateId)
        tag.putInt("Progress", progress)
        tag.putBoolean("Claimed", claimed)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): ActiveQuest {
            return ActiveQuest(
                templateId = tag.getString("TemplateId"),
                progress = tag.getInt("Progress"),
                claimed = tag.getBoolean("Claimed")
            )
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, ActiveQuest> =
            object : StreamCodec<FriendlyByteBuf, ActiveQuest> {
                override fun decode(buf: FriendlyByteBuf): ActiveQuest {
                    return ActiveQuest(
                        templateId = buf.readUtf(),
                        progress = buf.readVarInt(),
                        claimed = buf.readBoolean()
                    )
                }

                override fun encode(buf: FriendlyByteBuf, value: ActiveQuest) {
                    buf.writeUtf(value.templateId)
                    buf.writeVarInt(value.progress)
                    buf.writeBoolean(value.claimed)
                }
            }
    }
}

class QuestData(
    val dailyQuests: MutableList<ActiveQuest> = mutableListOf(),
    val weeklyQuests: MutableList<ActiveQuest> = mutableListOf(),
    var dailyResetDay: Long = 0L,
    var weeklyResetDay: Long = 0L,
    var dailyBonusClaimed: Boolean = false,
    var weeklyBonusClaimed: Boolean = false
) {
    fun getDailyClaimedCount(): Int = dailyQuests.count { it.claimed }
    fun getWeeklyClaimedCount(): Int = weeklyQuests.count { it.claimed }

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()

        val dailyList = ListTag()
        for (quest in dailyQuests) {
            dailyList.add(quest.toNBT())
        }
        tag.put("DailyQuests", dailyList)

        val weeklyList = ListTag()
        for (quest in weeklyQuests) {
            weeklyList.add(quest.toNBT())
        }
        tag.put("WeeklyQuests", weeklyList)

        tag.putLong("DailyResetDay", dailyResetDay)
        tag.putLong("WeeklyResetDay", weeklyResetDay)
        tag.putBoolean("DailyBonusClaimed", dailyBonusClaimed)
        tag.putBoolean("WeeklyBonusClaimed", weeklyBonusClaimed)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): QuestData {
            val data = QuestData()

            val dailyList = tag.getList("DailyQuests", 10)
            for (i in 0 until dailyList.size) {
                data.dailyQuests.add(ActiveQuest.fromNBT(dailyList.getCompound(i)))
            }

            val weeklyList = tag.getList("WeeklyQuests", 10)
            for (i in 0 until weeklyList.size) {
                data.weeklyQuests.add(ActiveQuest.fromNBT(weeklyList.getCompound(i)))
            }

            data.dailyResetDay = tag.getLong("DailyResetDay")
            data.weeklyResetDay = tag.getLong("WeeklyResetDay")
            data.dailyBonusClaimed = tag.getBoolean("DailyBonusClaimed")
            data.weeklyBonusClaimed = tag.getBoolean("WeeklyBonusClaimed")
            return data
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, QuestData> =
            object : StreamCodec<FriendlyByteBuf, QuestData> {
                override fun decode(buf: FriendlyByteBuf): QuestData {
                    val data = QuestData()

                    val dailyCount = buf.readVarInt()
                    repeat(dailyCount) {
                        data.dailyQuests.add(ActiveQuest.STREAM_CODEC.decode(buf))
                    }

                    val weeklyCount = buf.readVarInt()
                    repeat(weeklyCount) {
                        data.weeklyQuests.add(ActiveQuest.STREAM_CODEC.decode(buf))
                    }

                    data.dailyResetDay = buf.readLong()
                    data.weeklyResetDay = buf.readLong()
                    data.dailyBonusClaimed = buf.readBoolean()
                    data.weeklyBonusClaimed = buf.readBoolean()
                    return data
                }

                override fun encode(buf: FriendlyByteBuf, value: QuestData) {
                    buf.writeVarInt(value.dailyQuests.size)
                    for (quest in value.dailyQuests) {
                        ActiveQuest.STREAM_CODEC.encode(buf, quest)
                    }

                    buf.writeVarInt(value.weeklyQuests.size)
                    for (quest in value.weeklyQuests) {
                        ActiveQuest.STREAM_CODEC.encode(buf, quest)
                    }

                    buf.writeLong(value.dailyResetDay)
                    buf.writeLong(value.weeklyResetDay)
                    buf.writeBoolean(value.dailyBonusClaimed)
                    buf.writeBoolean(value.weeklyBonusClaimed)
                }
            }
    }
}
