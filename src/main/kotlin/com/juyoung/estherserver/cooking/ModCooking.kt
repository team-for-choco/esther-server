package com.juyoung.estherserver.cooking

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModCooking {
    val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EstherServerMod.MODID)

    val RECIPE_TYPES: DeferredRegister<RecipeType<*>> =
        DeferredRegister.create(Registries.RECIPE_TYPE, EstherServerMod.MODID)

    val RECIPE_SERIALIZERS: DeferredRegister<RecipeSerializer<*>> =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, EstherServerMod.MODID)

    val COOKING_STATION_BLOCK_ENTITY: DeferredHolder<BlockEntityType<*>, BlockEntityType<CookingStationBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("cooking_station", Supplier {
            BlockEntityType(::CookingStationBlockEntity, EstherServerMod.COOKING_STATION.get())
        })

    val COOKING_STATION_RECIPE_TYPE: DeferredHolder<RecipeType<*>, RecipeType<CookingStationRecipe>> =
        RECIPE_TYPES.register("cooking_station", Supplier {
            RecipeType.simple(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    EstherServerMod.MODID, "cooking_station"
                )
            )
        })

    val COOKING_STATION_RECIPE_SERIALIZER: DeferredHolder<RecipeSerializer<*>, RecipeSerializer<CookingStationRecipe>> =
        RECIPE_SERIALIZERS.register("cooking_station", Supplier {
            CookingStationRecipeSerializer()
        })

}
