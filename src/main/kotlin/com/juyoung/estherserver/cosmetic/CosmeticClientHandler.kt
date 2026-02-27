package com.juyoung.estherserver.cosmetic

import net.minecraft.world.entity.EquipmentSlot
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CosmeticClientHandler {

    // 자기 자신의 데이터
    var myUnlockedCosmetics: Set<String> = emptySet()
    var myEquipped: Map<EquipmentSlot, String> = emptyMap()

    // 다른 플레이어들의 장착 상태 (렌더링 Mixin에서 참조)
    val otherPlayersEquipped: ConcurrentHashMap<UUID, Map<EquipmentSlot, String>> = ConcurrentHashMap()

    private val SLOT_MAP = mapOf(
        "HEAD" to EquipmentSlot.HEAD,
        "CHEST" to EquipmentSlot.CHEST,
        "LEGS" to EquipmentSlot.LEGS,
        "FEET" to EquipmentSlot.FEET
    )

    fun handleSync(payload: CosmeticSyncPayload) {
        myUnlockedCosmetics = payload.unlockedCosmetics.toSet()
        myEquipped = payload.equipped.mapNotNull { (slotName, id) ->
            SLOT_MAP[slotName]?.let { it to id }
        }.toMap()
    }

    fun handleBroadcast(payload: CosmeticBroadcastPayload) {
        val equipped = payload.equipped.mapNotNull { (slotName, id) ->
            SLOT_MAP[slotName]?.let { it to id }
        }.toMap()

        if (equipped.isEmpty()) {
            otherPlayersEquipped.remove(payload.playerUUID)
        } else {
            otherPlayersEquipped[payload.playerUUID] = equipped
        }
    }

    /**
     * 특정 플레이어의 특정 슬롯 치장 ID를 반환.
     * Mixin에서 렌더링 시 사용.
     */
    fun getCosmeticForPlayer(playerUUID: UUID, slot: EquipmentSlot): String? {
        return otherPlayersEquipped[playerUUID]?.get(slot)
    }
}
