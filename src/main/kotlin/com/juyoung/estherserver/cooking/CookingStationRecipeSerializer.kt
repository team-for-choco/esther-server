package com.juyoung.estherserver.cooking

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer

class CookingStationRecipeSerializer : RecipeSerializer<CookingStationRecipe> {

    companion object {
        val CODEC: MapCodec<CookingStationRecipe> = RecordCodecBuilder.mapCodec { inst ->
            inst.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter { it.orderedIngredients },
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter { it.result }
            ).apply(inst, ::CookingStationRecipe)
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CookingStationRecipe> =
            object : StreamCodec<RegistryFriendlyByteBuf, CookingStationRecipe> {
                override fun decode(buf: RegistryFriendlyByteBuf): CookingStationRecipe {
                    val size = buf.readVarInt()
                    val ingredients = (0 until size).map { Ingredient.CONTENTS_STREAM_CODEC.decode(buf) }
                    val result = ItemStack.STREAM_CODEC.decode(buf)
                    return CookingStationRecipe(ingredients, result)
                }

                override fun encode(buf: RegistryFriendlyByteBuf, recipe: CookingStationRecipe) {
                    buf.writeVarInt(recipe.orderedIngredients.size)
                    recipe.orderedIngredients.forEach { Ingredient.CONTENTS_STREAM_CODEC.encode(buf, it) }
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result)
                }
            }
    }

    override fun codec(): MapCodec<CookingStationRecipe> = CODEC

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, CookingStationRecipe> = STREAM_CODEC
}
