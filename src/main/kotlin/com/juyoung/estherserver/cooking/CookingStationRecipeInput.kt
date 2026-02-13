package com.juyoung.estherserver.cooking

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeInput

class CookingStationRecipeInput(
    val items: List<ItemStack>
) : RecipeInput {
    override fun getItem(index: Int): ItemStack = items[index]

    override fun size(): Int = items.size
}
