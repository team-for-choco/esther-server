package com.juyoung.estherserver.sitting

import com.juyoung.estherserver.collection.CollectionScreen
import com.juyoung.estherserver.collection.TitleScreen
import com.juyoung.estherserver.inventory.ProfessionInventoryPayload
import com.juyoung.estherserver.profession.ProfessionScreen
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

    val PROFESSION_KEY = KeyMapping(
        "key.estherserver.profession",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        "key.categories.estherserver"
    )

    val PROFESSION_INVENTORY_KEY = KeyMapping(
        "key.estherserver.prof_inventory",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.estherserver"
    )

    val TITLE_KEY = KeyMapping(
        "key.estherserver.title",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
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
        while (PROFESSION_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(ProfessionScreen())
        }
        while (PROFESSION_INVENTORY_KEY.consumeClick()) {
            PacketDistributor.sendToServer(ProfessionInventoryPayload.OpenPayload())
        }
        while (TITLE_KEY.consumeClick()) {
            Minecraft.getInstance().setScreen(TitleScreen())
        }
    }
}
