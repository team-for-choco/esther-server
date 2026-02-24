package com.juyoung.estherserver.redstone

import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.BlockEvent

object RedstoneBlockHandler {

    private val REDSTONE_BLOCKED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath("estherserver", "redstone_blocked")
    )

    @SubscribeEvent
    fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
        val entity = event.entity ?: return
        val level = entity.level()
        if (level.isClientSide) return
        val serverPlayer = entity as? ServerPlayer ?: return

        if (event.placedBlock.`is`(REDSTONE_BLOCKED)) {
            event.isCanceled = true
            serverPlayer.inventoryMenu.sendAllDataToRemote()
            serverPlayer.displayClientMessage(
                Component.translatable("message.estherserver.redstone_blocked"), true
            )
        }
    }
}
