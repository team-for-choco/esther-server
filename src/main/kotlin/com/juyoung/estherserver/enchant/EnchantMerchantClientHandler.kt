package com.juyoung.estherserver.enchant

import net.minecraft.client.Minecraft

object EnchantMerchantClientHandler {

    fun handleOpen() {
        Minecraft.getInstance().setScreen(EnchantMerchantScreen())
    }

    fun handlePreview(payload: EnchantPreviewPayload) {
        EnchantMerchantScreen.pendingPreview = payload.enchants
        val screen = Minecraft.getInstance().screen
        if (screen is EnchantMerchantScreen) {
            screen.onPreviewReceived()
        }
    }

    fun handleDone() {
        val screen = Minecraft.getInstance().screen
        if (screen is EnchantMerchantScreen) {
            screen.onDone()
        }
    }
}
