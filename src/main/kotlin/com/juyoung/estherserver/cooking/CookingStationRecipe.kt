package com.juyoung.estherserver.cooking

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.PlacementInfo
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeBookCategory
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

class CookingStationRecipe(
    val orderedIngredients: List<Ingredient>,
    val result: ItemStack
) : Recipe<CookingStationRecipeInput> {

    override fun matches(input: CookingStationRecipeInput, level: Level): Boolean {
        val inputItems = input.items
        if (inputItems.size != orderedIngredients.size) return false

        for (i in orderedIngredients.indices) {
            if (!orderedIngredients[i].test(inputItems[i])) return false
        }
        return true
    }

    override fun assemble(input: CookingStationRecipeInput, registries: net.minecraft.core.HolderLookup.Provider): ItemStack {
        return result.copy()
    }

    override fun placementInfo(): PlacementInfo = PlacementInfo.NOT_PLACEABLE

    override fun recipeBookCategory(): RecipeBookCategory = RecipeBookCategory()

    override fun getSerializer(): RecipeSerializer<CookingStationRecipe> = ModCooking.COOKING_STATION_RECIPE_SERIALIZER.get()

    override fun getType(): RecipeType<CookingStationRecipe> = ModCooking.COOKING_STATION_RECIPE_TYPE.get()

    override fun isSpecial(): Boolean = true
}
