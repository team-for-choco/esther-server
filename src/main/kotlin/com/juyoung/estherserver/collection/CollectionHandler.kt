package com.juyoung.estherserver.collection

import com.juyoung.estherserver.economy.EconomyHandler
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
        // 제외 태그 확인
        if (stack.`is`(ModCollection.COLLECTION_EXCLUDED_TAG)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_not_collectible"), true
            )
            return false
        }

        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
        val key = CollectionKey(itemId)

        if (!CollectibleRegistry.isValidKey(key)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_not_collectible"), true
            )
            return false
        }

        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        if (data.isComplete(key)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.collection_already_registered"), true
            )
            player.level().playSound(
                null, player.blockPosition(),
                SoundEvents.VILLAGER_NO, SoundSource.PLAYERS,
                1.0f, 1.0f
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

        checkMilestones(player, data)

        return true
    }

    private fun checkMilestones(player: ServerPlayer, data: CollectionData) {
        var changed = false
        for (milestone in Milestone.entries) {
            if (milestone.id in data.notifiedMilestones) continue
            if (!milestone.check(data)) continue

            // 알림만 전송 — 칭호 해금(unlockedMilestones)은 보상 수령 시에만 수행
            data.notifiedMilestones.add(milestone.id)
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

    fun handleRewardClaim(player: ServerPlayer, milestoneId: String) {
        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        val milestone = Milestone.byId(milestoneId) ?: return

        // 달성 여부 확인
        if (!milestone.check(data)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.reward_not_achieved"), true
            )
            return
        }

        // 이미 수령 확인
        if (milestoneId in data.claimedRewards) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.reward_already_claimed"), true
            )
            return
        }

        // 칭호 보상
        val reward = milestone.reward
        if (reward.titleKey != null && milestoneId !in data.unlockedMilestones) {
            data.unlockedMilestones.add(milestoneId)
        }

        // 아이템 보상
        for (itemStack in reward.items) {
            val copy = itemStack.copy()
            if (!player.inventory.add(copy)) {
                player.drop(copy, false)
            }
        }

        // 화폐 보상
        if (reward.currencyReward > 0) {
            EconomyHandler.addBalance(player, reward.currencyReward)
        }

        data.claimedRewards.add(milestoneId)
        player.setData(ModCollection.COLLECTION_DATA.get(), data)
        syncToClient(player)

        player.displayClientMessage(
            Component.translatable("message.estherserver.reward_claimed"), false
        )

        player.level().playSound(
            null, player.blockPosition(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
            1.0f, 1.5f
        )
    }

    fun handleTitleSelect(player: ServerPlayer, milestoneId: String) {
        val data = player.getData(ModCollection.COLLECTION_DATA.get())

        if (milestoneId.isEmpty()) {
            data.activeTitle = null
            player.setData(ModCollection.COLLECTION_DATA.get(), data)
            syncToClient(player)
            ChatTitleHandler.applyTitleTeam(player)
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
        ChatTitleHandler.applyTitleTeam(player)

        val titleName = Component.translatable(milestone.titleKey)
        player.displayClientMessage(
            Component.translatable("message.estherserver.title_selected", titleName), false
        )
    }
}
