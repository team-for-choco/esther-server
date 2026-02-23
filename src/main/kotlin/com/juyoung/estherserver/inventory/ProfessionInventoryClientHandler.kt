package com.juyoung.estherserver.inventory

import net.minecraft.client.Minecraft

object ProfessionInventoryClientHandler {
    var cachedData: ProfessionInventoryData = ProfessionInventoryData()
        private set

    fun handleSync(payload: ProfessionInventoryPayload.SyncPayload) {
        cachedData = payload.data
        // Refresh screen if open
        val screen = Minecraft.getInstance().screen
        if (screen is ProfessionInventoryScreen) {
            screen.refreshData()
        }
    }
}
