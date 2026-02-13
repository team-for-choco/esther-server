package com.juyoung.estherserver.sitting

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.network.PacketDistributor
import org.lwjgl.glfw.GLFW

object ModKeyBindings {

    val SIT_KEY = KeyMapping(
        "key.estherserver.sit",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        "key.categories.estherserver"
    )

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        while (SIT_KEY.consumeClick()) {
            PacketDistributor.sendToServer(SitPayload())
        }
    }
}
