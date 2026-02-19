package com.juyoung.estherserver.merchant

import net.minecraft.client.Minecraft

object ShopClientHandler {
    fun handleOpenShop(payload: OpenShopPayload) {
        Minecraft.getInstance().setScreen(ShopScreen())
    }
}
