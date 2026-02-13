package com.juyoung.estherserver.collection

import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object CollectionHandler {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        PacketDistributor.sendToPlayer(player, CollectionSyncPayload(data))
    }

    fun tryRegisterItem(player: ServerPlayer, stack: ItemStack): Boolean {
        if (!stack.`is`(ModCollection.COLLECTIBLE_TAG)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_not_collectible"), true
            )
            return false
        }

        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
        val quality = stack.get(ModDataComponents.ITEM_QUALITY.get())
        val key = CollectionKey(itemId, quality)

        if (!CollectibleRegistry.isValidKey(key)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_invalid_quality"), true
            )
            return false
        }

        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        if (data.isComplete(key)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_already_registered"), true
            )
            return false
        }

        val gameTick = player.level().gameTime
        data.register(key, gameTick)
        player.setData(ModCollection.COLLECTION_DATA.get(), data)

        val entry = data.getEntry(key) ?: return true
        PacketDistributor.sendToPlayer(
            player,
            CollectionUpdatePayload(key, entry, data.getCompletedCount())
        )

        val itemName = stack.hoverName
        val total = CollectibleRegistry.getTotalCount()
        val completed = data.getCompletedCount()
        player.displayClientMessage(
            Component.translatable(
                "message.estherserver.collection_registered",
                itemName, completed, total
            ),
            false
        )

        player.level().playSound(
            null, player.blockPosition(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
            1.0f, 1.0f
        )

        checkMilestones(player, data)

        return true
    }

    private fun checkMilestones(player: ServerPlayer, data: CollectionData) {
        var changed = false
        for (milestone in Milestone.entries) {
            if (milestone.id in data.unlockedMilestones) continue
            if (!milestone.check(data)) continue

            data.unlockedMilestones.add(milestone.id)
            changed = true

            val titleName = Component.translatable(milestone.titleKey)
            val server = player.server
            server.playerList.broadcastSystemMessage(
                Component.translatable(
                    "message.estherserver.milestone_achieved",
                    player.displayName, titleName
                ),
                false
            )

            player.level().playSound(
                null, player.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
                1.0f, 2.0f
            )
        }
        if (changed) {
            player.setData(ModCollection.COLLECTION_DATA.get(), data)
            syncToClient(player)
        }
    }

    fun handleTitleSelect(player: ServerPlayer, milestoneId: String) {
        val data = player.getData(ModCollection.COLLECTION_DATA.get())

        if (milestoneId.isEmpty()) {
            data.activeTitle = null
            player.setData(ModCollection.COLLECTION_DATA.get(), data)
            syncToClient(player)
            player.refreshDisplayName()
            player.refreshTabListName()
            player.displayClientMessage(
                Component.translatable("message.estherserver.title_cleared"), false
            )
            return
        }

        val milestone = Milestone.byId(milestoneId)
        if (milestone == null || milestoneId !in data.unlockedMilestones) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.title_not_unlocked"), false
            )
            return
        }

        data.activeTitle = milestoneId
        player.setData(ModCollection.COLLECTION_DATA.get(), data)
        syncToClient(player)
        player.refreshDisplayName()
        player.refreshTabListName()

        val titleName = Component.translatable(milestone.titleKey)
        player.displayClientMessage(
            Component.translatable("message.estherserver.title_selected", titleName), false
        )
    }
}
