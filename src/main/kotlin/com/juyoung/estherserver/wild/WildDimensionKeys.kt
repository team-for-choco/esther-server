package com.juyoung.estherserver.wild

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType

object WildDimensionKeys {
    val WILD_LEVEL: ResourceKey<Level> = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "wild")
    )

    val WILD_TYPE: ResourceKey<DimensionType> = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "wild")
    )
}
