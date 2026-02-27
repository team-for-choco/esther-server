package com.juyoung.estherserver.cosmetic

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

// ── C→S: GUI 오픈 요청 ──
object RequestCosmeticsPayload : CustomPacketPayload {
    override fun type() = TYPE
    val TYPE = CustomPacketPayload.Type<RequestCosmeticsPayload>(
        ResourceLocation.fromNamespaceAndPath("estherserver", "request_cosmetics")
    )
    val STREAM_CODEC: StreamCodec<FriendlyByteBuf, RequestCosmeticsPayload> =
        object : StreamCodec<FriendlyByteBuf, RequestCosmeticsPayload> {
            override fun decode(buf: FriendlyByteBuf) = RequestCosmeticsPayload
            override fun encode(buf: FriendlyByteBuf, value: RequestCosmeticsPayload) {}
        }
}

// ── S→C: 해금 목록 + 장착 상태 동기화 (요청 플레이어 전용) ──
class CosmeticSyncPayload(
    val unlockedCosmetics: List<String>,
    val equipped: Map<String, String> // slotName → cosmeticId
) : CustomPacketPayload {
    override fun type() = TYPE
    companion object {
        val TYPE = CustomPacketPayload.Type<CosmeticSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "cosmetic_sync")
        )
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CosmeticSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, CosmeticSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): CosmeticSyncPayload {
                    val count = buf.readVarInt()
                    val unlocked = mutableListOf<String>()
                    repeat(count) { unlocked.add(buf.readUtf()) }
                    val equipCount = buf.readVarInt()
                    val equipped = mutableMapOf<String, String>()
                    repeat(equipCount) {
                        val slot = buf.readUtf()
                        val id = buf.readUtf()
                        equipped[slot] = id
                    }
                    return CosmeticSyncPayload(unlocked, equipped)
                }
                override fun encode(buf: FriendlyByteBuf, value: CosmeticSyncPayload) {
                    buf.writeVarInt(value.unlockedCosmetics.size)
                    for (id in value.unlockedCosmetics) buf.writeUtf(id)
                    buf.writeVarInt(value.equipped.size)
                    for ((slot, id) in value.equipped) {
                        buf.writeUtf(slot)
                        buf.writeUtf(id)
                    }
                }
            }
    }
}

// ── C→S: 장착/해제 요청 ──
class EquipCosmeticPayload(
    val slotName: String, // "HEAD", "CHEST", "LEGS", "FEET"
    val cosmeticId: String // 빈 문자열 = 해제
) : CustomPacketPayload {
    override fun type() = TYPE
    companion object {
        val TYPE = CustomPacketPayload.Type<EquipCosmeticPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "equip_cosmetic")
        )
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, EquipCosmeticPayload> =
            object : StreamCodec<FriendlyByteBuf, EquipCosmeticPayload> {
                override fun decode(buf: FriendlyByteBuf) =
                    EquipCosmeticPayload(buf.readUtf(), buf.readUtf())
                override fun encode(buf: FriendlyByteBuf, value: EquipCosmeticPayload) {
                    buf.writeUtf(value.slotName)
                    buf.writeUtf(value.cosmeticId)
                }
            }
    }
}

// ── S→All: 특정 플레이어의 장착 상태 브로드캐스트 (주변 플레이어에게) ──
class CosmeticBroadcastPayload(
    val playerUUID: UUID,
    val equipped: Map<String, String> // slotName → cosmeticId
) : CustomPacketPayload {
    override fun type() = TYPE
    companion object {
        val TYPE = CustomPacketPayload.Type<CosmeticBroadcastPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "cosmetic_broadcast")
        )
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CosmeticBroadcastPayload> =
            object : StreamCodec<FriendlyByteBuf, CosmeticBroadcastPayload> {
                override fun decode(buf: FriendlyByteBuf): CosmeticBroadcastPayload {
                    val uuid = buf.readUUID()
                    val count = buf.readVarInt()
                    val equipped = mutableMapOf<String, String>()
                    repeat(count) {
                        val slot = buf.readUtf()
                        val id = buf.readUtf()
                        equipped[slot] = id
                    }
                    return CosmeticBroadcastPayload(uuid, equipped)
                }
                override fun encode(buf: FriendlyByteBuf, value: CosmeticBroadcastPayload) {
                    buf.writeUUID(value.playerUUID)
                    buf.writeVarInt(value.equipped.size)
                    for ((slot, id) in value.equipped) {
                        buf.writeUtf(slot)
                        buf.writeUtf(id)
                    }
                }
            }
    }
}
