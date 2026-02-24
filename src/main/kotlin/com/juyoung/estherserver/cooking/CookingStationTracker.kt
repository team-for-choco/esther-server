package com.juyoung.estherserver.cooking

import net.minecraft.core.BlockPos
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CookingStationTracker {
    // playerUUID -> set of active cooking station positions
    private val activeCookingStations = ConcurrentHashMap<UUID, MutableSet<BlockPos>>()

    fun getActiveCookingCount(playerUUID: UUID): Int =
        activeCookingStations[playerUUID]?.size ?: 0

    fun addActiveCooking(playerUUID: UUID, pos: BlockPos) {
        activeCookingStations.getOrPut(playerUUID) { ConcurrentHashMap.newKeySet() }.add(pos)
    }

    fun removeActiveCooking(playerUUID: UUID, pos: BlockPos) {
        val stations = activeCookingStations[playerUUID] ?: return
        stations.remove(pos)
        if (stations.isEmpty()) {
            activeCookingStations.remove(playerUUID)
        }
    }

    /** Max concurrent cooking stations based on cooking tool enhancement level */
    fun getMaxConcurrentStations(equipLevel: Int): Int = when {
        equipLevel >= 3 -> 4
        equipLevel >= 2 -> 2
        else -> 1
    }
}
