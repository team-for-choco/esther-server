package com.juyoung.estherserver.sitting

import com.juyoung.estherserver.collection.CollectionScreen
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
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

    val COLLECTION_KEY = KeyMapping(
        "key.estherserver.collection",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        "key.categories.estherserver"
    )

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        while (SIT_KEY.consumeClick()) {
            PacketDistributor.sendToServer(SitPayload)
        }
        while (COLLECTION_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(CollectionScreen())
        }
    }
}
