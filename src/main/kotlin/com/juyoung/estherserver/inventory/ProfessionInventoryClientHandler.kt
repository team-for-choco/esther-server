package com.juyoung.estherserver.inventory

import net.minecraft.client.Minecraft

object ProfessionInventoryClientHandler {
    var cachedData: ProfessionInventoryData = ProfessionInventoryData()
        private set

    fun handleSync(payload: ProfessionInventoryPayload.SyncPayload) {
        cachedData = payload.data
    }

    fun handleTabSync(payload: ProfessionInventoryPayload.TabSyncPayload) {
        val screen = Minecraft.getInstance().screen
        if (screen is ProfessionInventoryContainerScreen) {
            screen.handleTabSync(payload.tab, payload.unlockedSlots)
        }
    }
}
