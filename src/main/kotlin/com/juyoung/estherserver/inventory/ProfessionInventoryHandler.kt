package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionHandler
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object ProfessionInventoryHandler {

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
    private fun getProfessionForItem(stack: ItemStack): Profession? {
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

    /** Handle slot move request from client */
    fun handleSlotMove(player: ServerPlayer, fromProfession: Int, fromSlot: Int, toProfession: Int, toSlot: Int) {
        val data = getData(player)

        val fromProf = if (fromProfession >= 0 && fromProfession < Profession.entries.size) Profession.entries[fromProfession] else null
        val toProf = if (toProfession >= 0 && toProfession < Profession.entries.size) Profession.entries[toProfession] else null

        if (fromProf == null || toProf == null) return

        val fromItem = data.getItem(fromProf, fromSlot)
        val toItem = data.getItem(toProf, toSlot)

        // Check slot availability
        val toAvailable = getAvailableSlots(player, toProf)
        val fromAvailable = getAvailableSlots(player, fromProf)

        if (toSlot >= toAvailable || fromSlot >= fromAvailable) return

        // Swap items
        data.setItem(fromProf, fromSlot, toItem)
        data.setItem(toProf, toSlot, fromItem)

        saveData(player, data)
        syncToClient(player)
    }
}
