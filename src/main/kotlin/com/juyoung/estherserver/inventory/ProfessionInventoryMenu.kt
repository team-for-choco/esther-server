package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.profession.Profession
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import com.juyoung.estherserver.enhancement.EnhancementHandler
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor

class ProfessionInventoryMenu(
    containerId: Int,
    playerInventory: Inventory
) : AbstractContainerMenu(ModInventory.PROFESSION_INVENTORY_MENU.get(), containerId) {

    companion object {
        const val PROFESSION_SLOT_COUNT = 25
        const val TOOL_SLOT_INDEX = 25
        const val TOTAL_PROFESSION_SLOTS = 26 // 25 storage + 1 tool
        const val SLOTS_PER_ROW = 5
        const val ROWS = 5

        const val PROFESSION_SLOT_X = 95
        const val PROFESSION_SLOT_Y = 40
        const val TOOL_SLOT_X = 55
        const val TOOL_SLOT_Y = 75
        const val PLAYER_INV_X = 59
        const val PLAYER_INV_Y = 142
        const val HOTBAR_Y = 200
    }

    val professionContainer = SimpleContainer(PROFESSION_SLOT_COUNT)
    val toolContainer = SimpleContainer(1)
    private val player: Player = playerInventory.player

    var currentTab = 0
    var unlockedSlots = 0

    init {
        // Profession slots (5x5 grid)
        for (row in 0 until ROWS) {
            for (col in 0 until SLOTS_PER_ROW) {
                val index = row * SLOTS_PER_ROW + col
                addSlot(ProfessionSlot(
                    professionContainer, index,
                    PROFESSION_SLOT_X + col * 18,
                    PROFESSION_SLOT_Y + row * 18,
                    this
                ))
            }
        }

        // Tool slot (index 25)
        addSlot(ProfessionToolSlot(
            toolContainer, 0,
            TOOL_SLOT_X, TOOL_SLOT_Y,
            this
        ))

        // Player inventory (3x9)
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInventory, col + row * 9 + 9,
                    PLAYER_INV_X + col * 18,
                    PLAYER_INV_Y + row * 18))
            }
        }

        // Hotbar (1x9)
        for (col in 0 until 9) {
            addSlot(Slot(playerInventory, col,
                PLAYER_INV_X + col * 18,
                HOTBAR_Y))
        }

        // Load initial tab data (tab 0 = MINING)
        loadCurrentTabFromData()
    }

    fun getCurrentProfession(): Profession = when (currentTab) {
        0 -> Profession.MINING
        1 -> Profession.FISHING
        2 -> Profession.FARMING
        3 -> Profession.COOKING
        else -> Profession.MINING
    }

    fun switchTab(newTab: Int) {
        if (newTab < 0 || newTab > 3) return
        if (newTab == currentTab) return

        saveCurrentTabToData()
        currentTab = newTab
        loadCurrentTabFromData()

        val serverPlayer = player as? ServerPlayer
        if (serverPlayer != null) {
            PacketDistributor.sendToPlayer(
                serverPlayer,
                ProfessionInventoryPayload.TabSyncPayload(currentTab, unlockedSlots)
            )
        }
        broadcastChanges()
    }

    private fun saveCurrentTabToData() {
        val serverPlayer = player as? ServerPlayer ?: return
        val profession = getCurrentProfession()

        val data = ProfessionInventoryHandler.getData(serverPlayer)
        for (i in 0 until PROFESSION_SLOT_COUNT) {
            data.setItem(profession, i, professionContainer.getItem(i))
        }
        data.setTool(profession, toolContainer.getItem(0))
        ProfessionInventoryHandler.saveData(serverPlayer, data)
    }

    private fun loadCurrentTabFromData() {
        val serverPlayer = player as? ServerPlayer
        val profession = getCurrentProfession()

        if (serverPlayer != null) {
            unlockedSlots = ProfessionInventoryHandler.getAvailableSlots(serverPlayer, profession)
            val data = ProfessionInventoryHandler.getData(serverPlayer)
            for (i in 0 until PROFESSION_SLOT_COUNT) {
                professionContainer.setItem(i, data.getItem(profession, i).copy())
            }
            toolContainer.setItem(0, data.getTool(profession).copy())
        } else {
            unlockedSlots = 0
            for (i in 0 until PROFESSION_SLOT_COUNT) {
                professionContainer.setItem(i, ItemStack.EMPTY)
            }
            toolContainer.setItem(0, ItemStack.EMPTY)
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY

        val stack = slot.item
        val original = stack.copy()

        val playerSlotsStart = TOTAL_PROFESSION_SLOTS
        val playerSlotsEnd = TOTAL_PROFESSION_SLOTS + 36

        if (index < PROFESSION_SLOT_COUNT) {
            // Profession storage slot -> Player inventory
            if (!moveItemStackTo(stack, playerSlotsStart, playerSlotsEnd, true)) {
                return ItemStack.EMPTY
            }
        } else if (index == TOOL_SLOT_INDEX) {
            // Tool slot -> Player inventory
            if (!moveItemStackTo(stack, playerSlotsStart, playerSlotsEnd, true)) {
                return ItemStack.EMPTY
            }
        } else {
            // Player inventory -> try tool slot first for special tools, then profession slots
            if (isSpecialToolForCurrentTab(stack)) {
                if (!moveItemStackTo(stack, TOOL_SLOT_INDEX, TOOL_SLOT_INDEX + 1, false)) {
                    // Tool slot full, try profession slots
                    if (!moveItemStackTo(stack, 0, PROFESSION_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else {
                if (!moveItemStackTo(stack, 0, PROFESSION_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY
                }
            }
        }

        if (stack.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }

        if (stack.count == original.count) {
            return ItemStack.EMPTY
        }

        slot.onTake(player, stack)
        return original
    }

    private fun isSpecialToolForCurrentTab(stack: ItemStack): Boolean {
        val profession = getCurrentProfession()
        val expectedItem = EnhancementHandler.EQUIPMENT_MAP[profession]?.get() ?: return false
        return stack.item === expectedItem
    }

    override fun stillValid(player: Player): Boolean = true

    override fun removed(player: Player) {
        if (player is ServerPlayer) {
            saveCurrentTabToData()
        }
        super.removed(player)
    }
}
