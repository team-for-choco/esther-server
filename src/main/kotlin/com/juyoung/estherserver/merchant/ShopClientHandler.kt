package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.enhancement.EnhancementScreen
import net.minecraft.client.Minecraft

object ShopClientHandler {
    fun handleOpenShop(payload: OpenShopPayload) {
        val merchantType = try {
            ShopCategory.valueOf(payload.merchantType)
        } catch (_: IllegalArgumentException) {
            ShopCategory.SEEDS
        }

        if (merchantType == ShopCategory.BLACKSMITH) {
            Minecraft.getInstance().setScreen(EnhancementScreen())
        } else {
            Minecraft.getInstance().setScreen(ShopScreen(merchantType, payload.entityId))
        }
    }
}
