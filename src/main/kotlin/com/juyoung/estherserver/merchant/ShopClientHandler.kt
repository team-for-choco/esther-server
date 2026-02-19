package com.juyoung.estherserver.merchant

import net.minecraft.client.Minecraft

object ShopClientHandler {
    fun handleOpenShop(payload: OpenShopPayload) {
        val merchantType = try {
            ShopCategory.valueOf(payload.merchantType)
        } catch (_: IllegalArgumentException) {
            ShopCategory.SEEDS
        }
        Minecraft.getInstance().setScreen(ShopScreen(merchantType))
    }
}
