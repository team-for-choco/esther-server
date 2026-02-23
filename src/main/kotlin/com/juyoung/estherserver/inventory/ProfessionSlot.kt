package com.juyoung.estherserver.inventory

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class ProfessionSlot(
    container: Container,
    index: Int,
    x: Int,
    y: Int,
    private val menu: ProfessionInventoryMenu
) : Slot(container, index, x, y) {

    override fun isActive(): Boolean {
        if (menu.currentTab == 0) return false
        return containerSlot < menu.unlockedSlots
    }

    override fun mayPlace(stack: ItemStack): Boolean {
        if (!isActive()) return false
        val profession = menu.getCurrentProfession() ?: return false
        val itemProfession = ProfessionInventoryHandler.getProfessionForItem(stack)
        return itemProfession == null || itemProfession == profession
    }
}
