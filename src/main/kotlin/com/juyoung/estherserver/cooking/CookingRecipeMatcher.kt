package com.juyoung.estherserver.cooking

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object CookingRecipeMatcher {
    fun findMatchingRecipe(level: Level, ingredients: List<ItemStack>): ItemStack? {
        val serverLevel = level as? ServerLevel ?: return null
        val recipeManager = serverLevel.server.recipeManager
        val input = CookingStationRecipeInput(ingredients)

        val recipe = recipeManager.recipeMap()
            .byType(ModCooking.COOKING_STATION_RECIPE_TYPE.get())
            .firstOrNull { it.value().matches(input, level) }

        return recipe?.value()?.assemble(input, level.registryAccess())
    }
}
