package com.juyoung.estherserver.block

import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import java.util.function.Supplier

class CustomCropBlock(
    properties: BlockBehaviour.Properties,
    private val seedSupplier: Supplier<out ItemLike>
) : CropBlock(properties) {

    override fun getBaseSeedId(): ItemLike = seedSupplier.get()
}
