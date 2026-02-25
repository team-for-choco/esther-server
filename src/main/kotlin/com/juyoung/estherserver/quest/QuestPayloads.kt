package com.juyoung.estherserver.quest

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class QuestSyncPayload(val data: QuestData) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<QuestSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "quest_sync")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, QuestSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, QuestSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): QuestSyncPayload {
                    return QuestSyncPayload(QuestData.STREAM_CODEC.decode(buf))
                }

                override fun encode(buf: FriendlyByteBuf, value: QuestSyncPayload) {
                    QuestData.STREAM_CODEC.encode(buf, value.data)
                }
            }
    }
}

class QuestClaimPayload(val questIndex: Int, val isWeekly: Boolean) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<QuestClaimPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "quest_claim")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, QuestClaimPayload> =
            object : StreamCodec<FriendlyByteBuf, QuestClaimPayload> {
                override fun decode(buf: FriendlyByteBuf): QuestClaimPayload {
                    return QuestClaimPayload(buf.readVarInt(), buf.readBoolean())
                }

                override fun encode(buf: FriendlyByteBuf, value: QuestClaimPayload) {
                    buf.writeVarInt(value.questIndex)
                    buf.writeBoolean(value.isWeekly)
                }
            }
    }
}

class QuestBonusClaimPayload(val isWeekly: Boolean) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<QuestBonusClaimPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "quest_bonus_claim")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, QuestBonusClaimPayload> =
            object : StreamCodec<FriendlyByteBuf, QuestBonusClaimPayload> {
                override fun decode(buf: FriendlyByteBuf): QuestBonusClaimPayload {
                    return QuestBonusClaimPayload(buf.readBoolean())
                }

                override fun encode(buf: FriendlyByteBuf, value: QuestBonusClaimPayload) {
                    buf.writeBoolean(value.isWeekly)
                }
            }
    }
}

class QuestOpenScreenPayload(val data: QuestData) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<QuestOpenScreenPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "quest_open_screen")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, QuestOpenScreenPayload> =
            object : StreamCodec<FriendlyByteBuf, QuestOpenScreenPayload> {
                override fun decode(buf: FriendlyByteBuf): QuestOpenScreenPayload {
                    return QuestOpenScreenPayload(QuestData.STREAM_CODEC.decode(buf))
                }

                override fun encode(buf: FriendlyByteBuf, value: QuestOpenScreenPayload) {
                    QuestData.STREAM_CODEC.encode(buf, value.data)
                }
            }
    }
}
