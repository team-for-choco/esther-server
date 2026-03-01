package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.enhancement.EnhancementHandler
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class ProfessionToolSlot(
    container: Container,
    index: Int,
    x: Int,
    y: Int,
    private val menu: ProfessionInventoryMenu
) : Slot(container, index, x, y) {

    override fun mayPlace(stack: ItemStack): Boolean {
        val profession = menu.getCurrentProfession()
        val expectedItem = EnhancementHandler.EQUIPMENT_MAP[profession]?.get() ?: return false
        return stack.item === expectedItem
    }

    override fun getMaxStackSize(): Int = 1
}
