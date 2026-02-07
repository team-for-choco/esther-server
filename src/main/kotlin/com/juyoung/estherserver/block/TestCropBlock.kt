package com.juyoung.estherserver.block

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

class TestCropBlock(properties: BlockBehaviour.Properties) : CropBlock(properties) {

    override fun getBaseSeedId(): ItemLike = EstherServerMod.TEST_SEEDS.get()

    override fun getMaxAge(): Int = MAX_AGE

    override fun getAgeProperty(): IntegerProperty = AGE

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE)
    }

    companion object {
        const val MAX_AGE = 7
        val AGE: IntegerProperty = BlockStateProperties.AGE_7
    }
}
