package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.profession.Profession
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 인벤토리 세이브권 핸들러.
 * 사망 시 세이브권이 있으면 1개 소모 → 인벤토리/전문 보관함/경험치 보존.
 *
 * 바닐라 MC는 사망 처리 중 인벤토리와 경험치를 비우므로,
 * LivingDeathEvent 시점에 스냅샷을 저장해두고 PlayerEvent.Clone에서 복원한다.
 */
object InventorySaveHandler {

    private data class SavedData(
        val inventoryContents: List<ItemStack>,
        val professionData: ProfessionInventoryData,
        val experienceLevel: Int,
        val experienceProgress: Float,
        val totalExperience: Int
    )

    private val savedPlayers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val savedData: ConcurrentHashMap<UUID, SavedData> = ConcurrentHashMap()

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

                // 인벤토리 스냅샷 저장 (바닐라가 비우기 전에)
                val inventoryCopy = mutableListOf<ItemStack>()
                for (j in 0 until player.inventory.containerSize) {
                    inventoryCopy.add(player.inventory.getItem(j).copy())
                }

                // 전문 보관함 스냅샷
                val profData = player.getData(ModInventory.PROFESSION_INVENTORY.get())
                val profCopy = ProfessionInventoryData()
                for (profession in Profession.entries) {
                    val tool = profData.getTool(profession)
                    if (!tool.isEmpty) {
                        profCopy.setTool(profession, tool.copy())
                    }
                    val items = profData.getItems(profession)
                    for ((index, item) in items.withIndex()) {
                        if (!item.isEmpty) {
                            profCopy.setItem(profession, index, item.copy())
                        }
                    }
                }

                savedData[player.uuid] = SavedData(
                    inventoryContents = inventoryCopy,
                    professionData = profCopy,
                    experienceLevel = player.experienceLevel,
                    experienceProgress = player.experienceProgress,
                    totalExperience = player.totalExperience
                )

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
    fun onExperienceDrop(event: LivingExperienceDropEvent) {
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

        val data = savedData.remove(original.uuid) ?: run {
            savedPlayers.remove(original.uuid)
            return
        }
        savedPlayers.remove(original.uuid)

        // 저장된 스냅샷에서 인벤토리 복원
        for (i in data.inventoryContents.indices) {
            if (i < newPlayer.inventory.containerSize) {
                newPlayer.inventory.setItem(i, data.inventoryContents[i])
            }
        }

        // 전문 보관함 복원
        ProfessionInventoryHandler.saveData(newPlayer, data.professionData)

        // 경험치 복원
        newPlayer.experienceLevel = data.experienceLevel
        newPlayer.experienceProgress = data.experienceProgress
        newPlayer.totalExperience = data.totalExperience
    }
}
