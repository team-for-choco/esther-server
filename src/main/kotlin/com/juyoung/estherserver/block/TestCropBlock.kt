package com.juyoung.estherserver.block

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class TestCropBlock(properties: BlockBehaviour.Properties) : CropBlock(properties) {

    override fun getBaseSeedId(): ItemLike = EstherServerMod.TEST_SEEDS.get()
}
