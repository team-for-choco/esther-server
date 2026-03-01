package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionHandler
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent
import net.neoforged.neoforge.event.entity.item.ItemTossEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object ProfessionInventoryHandler {

    fun isSpecialTool(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        return EnhancementHandler.EQUIPMENT_MAP.values.any { it.get() === stack.item }
    }

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
    fun onItemToss(event: ItemTossEvent) {
        val stack = event.entity.item
        if (isSpecialTool(stack)) {
            event.isCanceled = true
            val player = event.player
            if (!player.inventory.add(stack)) {
                // 인벤토리에 자리가 없으면 원래 슬롯에 강제 복원 시도
                player.inventory.add(stack)
            }
            if (player is ServerPlayer) {
                player.sendSystemMessage(
                    Component.translatable("message.estherserver.special_tool_no_drop")
                )
            }
        }
    }

    @SubscribeEvent
    fun onLivingDrops(event: LivingDropsEvent) {
        val player = event.entity as? ServerPlayer ?: return
        if (player.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return

        event.drops.removeIf { itemEntity ->
            isSpecialTool(itemEntity.item)
        }
    }

    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        if (!event.isWasDeath) return // 엔드 포탈 등 — 사망 아님
        val original = event.original as? ServerPlayer ?: return
        val newPlayer = event.entity as? ServerPlayer ?: return

        if (original.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return

        // 이전 플레이어 인벤토리에서 특수 도구 수집
        val toolsToRestore = mutableListOf<ItemStack>()
        for (stack in original.inventory.items) {
            if (isSpecialTool(stack)) {
                toolsToRestore.add(stack.copy())
            }
        }
        // 전문 보관함 도구 슬롯에서도 수집
        val profData = original.getData(ModInventory.PROFESSION_INVENTORY.get())
        for (profession in Profession.entries) {
            val tool = profData.getTool(profession)
            if (!tool.isEmpty && isSpecialTool(tool)) {
                toolsToRestore.add(tool.copy())
            }
        }

        // 새 플레이어에게 복원
        for (tool in toolsToRestore) {
            newPlayer.inventory.add(tool)
        }
    }

    @SubscribeEvent
    fun onPlayerDeath(event: LivingDeathEvent) {
        val player = event.entity as? ServerPlayer ?: return
        if (player.server.gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY)) return

        val data = getData(player)
        for (profession in Profession.entries) {
            val items = data.getItems(profession)
            for (stack in items) {
                if (!stack.isEmpty) {
                    player.drop(stack.copy(), true, false)
                }
            }
            // 도구 슬롯은 드롭하지 않음 (Clone에서 복원)
        }
        // 전문 보관함 초기화 시 도구 슬롯도 보존
        val newData = ProfessionInventoryData()
        for (profession in Profession.entries) {
            val tool = data.getTool(profession)
            if (!tool.isEmpty) {
                newData.setTool(profession, tool.copy())
            }
        }
        saveData(player, newData)
    }

    fun getAvailableSlots(player: ServerPlayer, profession: Profession): Int {
        val profLevel = ProfessionHandler.getLevel(player, profession)
        return ProfessionBonusHelper.getInventorySlots(profLevel)
    }

    fun getData(player: ServerPlayer): ProfessionInventoryData {
        return player.getData(ModInventory.PROFESSION_INVENTORY.get())
    }

    fun saveData(player: ServerPlayer, data: ProfessionInventoryData) {
        player.setData(ModInventory.PROFESSION_INVENTORY.get(), data)
    }

    /** Try to add an item to the appropriate profession inventory. Returns true if successful. */
    fun tryAddItem(player: ServerPlayer, stack: ItemStack): Boolean {
        val profession = getProfessionForItem(stack) ?: return false
        val availableSlots = getAvailableSlots(player, profession)
        if (availableSlots <= 0) return false

        val data = getData(player)
        val success = data.tryAddItem(profession, stack, availableSlots)
        if (success) {
            saveData(player, data)
        }
        return success
    }

    /** Determine which profession an item belongs to (for auto-sorting) */
    fun getProfessionForItem(stack: ItemStack): Profession? {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item)
        // Check fish
        if (ProfessionBonusHelper.getFishGrade(itemId) != null) return Profession.FISHING
        // Check crops
        if (ProfessionBonusHelper.getCropGrade(itemId) != null) return Profession.FARMING
        // Check ores
        if (ProfessionBonusHelper.getOreGrade(itemId) != null) return Profession.MINING
        // Check by profession handler
        return com.juyoung.estherserver.profession.ProfessionHandler.getProfessionForItem(stack)
    }

    fun syncToClient(player: ServerPlayer) {
        val data = getData(player)
        PacketDistributor.sendToPlayer(player, ProfessionInventoryPayload.SyncPayload(data))
    }

}
