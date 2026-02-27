package com.juyoung.estherserver.cosmetic

import net.minecraft.client.Minecraft
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
     * 브로드캐스트 캐시 → 로컬 플레이어 myEquipped 순으로 조회.
     */
    fun getCosmeticForPlayer(playerUUID: UUID, slot: EquipmentSlot): String? {
        // 브로드캐스트로 받은 데이터 우선
        otherPlayersEquipped[playerUUID]?.get(slot)?.let { return it }
        // 로컬 플레이어 fallback (브로드캐스트 수신 전에도 자기 치장 보이도록)
        val localPlayer = Minecraft.getInstance().player
        if (localPlayer != null && localPlayer.uuid == playerUUID) {
            return myEquipped[slot]
        }
        return null
    }
}
