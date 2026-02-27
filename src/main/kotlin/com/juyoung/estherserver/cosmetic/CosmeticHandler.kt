package com.juyoung.estherserver.cosmetic

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EquipmentSlot
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object CosmeticHandler {

    private val VALID_SLOTS = mapOf(
        "HEAD" to EquipmentSlot.HEAD,
        "CHEST" to EquipmentSlot.CHEST,
        "LEGS" to EquipmentSlot.LEGS,
        "FEET" to EquipmentSlot.FEET
    )

    /**
     * GUI 오픈 요청 처리: 해금 목록 + 장착 상태를 클라이언트에 전송
     */
    fun handleRequest(player: ServerPlayer) {
        val data = player.getData(ModCosmetics.COSMETIC_DATA.get())
        val equipped = data.equipped.map { (slot, id) -> slot.name to id }.toMap()
        PacketDistributor.sendToPlayer(
            player,
            CosmeticSyncPayload(data.unlockedCosmetics.toList(), equipped)
        )
    }

    /**
     * 장착/해제 요청 처리
     */
    fun handleEquip(player: ServerPlayer, slotName: String, cosmeticId: String) {
        val slot = VALID_SLOTS[slotName] ?: return
        val data = player.getData(ModCosmetics.COSMETIC_DATA.get())

        if (cosmeticId.isEmpty()) {
            // 해제
            data.equipped.remove(slot)
        } else {
            // 장착: 해금 여부 + 슬롯 일치 검증
            if (cosmeticId !in data.unlockedCosmetics) return
            val def = CosmeticRegistry.get(cosmeticId) ?: return
            if (def.slot != slot) return
            data.equipped[slot] = cosmeticId
        }

        player.setData(ModCosmetics.COSMETIC_DATA.get(), data)

        // 요청 플레이어에게 동기화
        val equipped = data.equipped.map { (s, id) -> s.name to id }.toMap()
        PacketDistributor.sendToPlayer(player, CosmeticSyncPayload(data.unlockedCosmetics.toList(), equipped))

        // 주변 플레이어에게 브로드캐스트
        broadcastToNearby(player)
    }

    /**
     * 주변 플레이어들에게 장착 상태 브로드캐스트
     */
    fun broadcastToNearby(player: ServerPlayer) {
        val data = player.getData(ModCosmetics.COSMETIC_DATA.get())
        val equipped = data.equipped.map { (slot, id) -> slot.name to id }.toMap()
        val payload = CosmeticBroadcastPayload(player.uuid, equipped)

        // 같은 레벨의 모든 플레이어에게 전송 (자기 자신 포함)
        val server = player.server
        for (otherPlayer in server.playerList.players) {
            if (otherPlayer.level() == player.level()) {
                PacketDistributor.sendToPlayer(otherPlayer, payload)
            }
        }
    }

    /**
     * 로그인 시 기존 장착 상태를 모든 플레이어에게 브로드캐스트
     */
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        // 약간의 딜레이 후 브로드캐스트 (로그인 완료 후)
        player.server.execute {
            broadcastToNearby(player)

            // 이미 접속 중인 다른 플레이어들의 치장 상태도 이 플레이어에게 전송
            for (otherPlayer in player.server.playerList.players) {
                if (otherPlayer != player) {
                    val otherData = otherPlayer.getData(ModCosmetics.COSMETIC_DATA.get())
                    if (otherData.equipped.isNotEmpty()) {
                        val otherEquipped = otherData.equipped.map { (slot, id) -> slot.name to id }.toMap()
                        PacketDistributor.sendToPlayer(
                            player,
                            CosmeticBroadcastPayload(otherPlayer.uuid, otherEquipped)
                        )
                    }
                }
            }
        }
    }
}
