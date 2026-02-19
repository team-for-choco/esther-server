package com.juyoung.estherserver.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

class CustomCropBlock(
    properties: BlockBehaviour.Properties,
    private val seedSupplier: Supplier<out ItemLike>
) : CropBlock(properties) {

    override fun getBaseSeedId(): ItemLike = seedSupplier.get()

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.block is SpecialFarmlandBlock
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block) && isMaxAge(state)) {
            val belowPos = pos.below()
            val belowState = level.getBlockState(belowPos)
            if (belowState.block is SpecialFarmlandBlock &&
                belowState.getValue(SpecialFarmlandBlock.MOISTURE) == 1
            ) {
                level.setBlock(belowPos, belowState.setValue(SpecialFarmlandBlock.MOISTURE, 0), 3)
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }
}
