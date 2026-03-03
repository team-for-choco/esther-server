package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.profession.Profession
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import java.util.UUID

object InventorySaveHandler {

    private val savedPlayers: MutableSet<UUID> = mutableSetOf()

    fun isSaved(player: ServerPlayer): Boolean = savedPlayers.contains(player.uuid)

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: LivingDeathEvent) {
        val player = event.entity as? ServerPlayer ?: return
        if (player.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return

        val ticketItem = EstherServerMod.INVENTORY_SAVE_TICKET.get()

        // 인벤토리에서 세이브권 검색
        for (i in 0 until player.inventory.containerSize) {
            val stack = player.inventory.getItem(i)
            if (!stack.isEmpty && stack.item === ticketItem) {
                // 1개 소모
                stack.shrink(1)
                savedPlayers.add(player.uuid)
                player.sendSystemMessage(
                    Component.translatable("message.estherserver.inventory_saved")
                )
                return
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onLivingDrops(event: LivingDropsEvent) {
        val player = event.entity as? ServerPlayer ?: return
        if (savedPlayers.contains(player.uuid)) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPlayerClone(event: PlayerEvent.Clone) {
        if (!event.isWasDeath) return
        val original = event.original as? ServerPlayer ?: return
        val newPlayer = event.entity as? ServerPlayer ?: return

        if (!savedPlayers.remove(original.uuid)) return

        // 인벤토리 전체 복사
        newPlayer.inventory.replaceWith(original.inventory)

        // 전문 보관함 데이터 복사
        val originalProfData = original.getData(ModInventory.PROFESSION_INVENTORY.get())
        val newProfData = ProfessionInventoryData()
        for (profession in Profession.entries) {
            val tool = originalProfData.getTool(profession)
            if (!tool.isEmpty) {
                newProfData.setTool(profession, tool.copy())
            }
            val items = originalProfData.getItems(profession)
            for ((index, stack) in items.withIndex()) {
                if (!stack.isEmpty) {
                    newProfData.setItem(profession, index, stack.copy())
                }
            }
        }
        ProfessionInventoryHandler.saveData(newPlayer, newProfData)

        // 경험치 복사
        newPlayer.experienceLevel = original.experienceLevel
        newPlayer.experienceProgress = original.experienceProgress
        newPlayer.totalExperience = original.totalExperience
    }
}
