package com.juyoung.estherserver.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.neoforged.neoforge.common.util.TriState
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SpecialFarmlandBlock(properties: Properties) : Block(properties) {
    companion object {
        val MOISTURE: IntegerProperty = IntegerProperty.create("moisture", 0, 1)
        val SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(MOISTURE, 0))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(MOISTURE)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = SHAPE

    override fun useShapeForLightOcclusion(state: BlockState): Boolean = true

    override fun canSustainPlant(
        state: BlockState,
        level: BlockGetter,
        soilPosition: BlockPos,
        facing: Direction,
        plant: BlockState
    ): TriState {
        return if (facing == Direction.UP) TriState.TRUE else TriState.DEFAULT
    }

    override fun isFertile(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.getValue(MOISTURE) == 1
    }

    override fun fallOn(
        level: net.minecraft.world.level.Level,
        state: BlockState,
        pos: BlockPos,
        entity: net.minecraft.world.entity.Entity,
        fallDistance: Float
    ) {
        // Do NOT convert to dirt when trampled (unlike vanilla FarmBlock)
        entity.causeFallDamage(fallDistance, 1.0f, entity.damageSources().fall())
    }
}
