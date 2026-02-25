package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.sitting.SeatEntity
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult

class CatSofaDummyBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val PART = IntegerProperty.create("part", 0, 6)
        val CODEC: MapCodec<CatSofaDummyBlock> = simpleCodec(::CatSofaDummyBlock)

        // Seat parts: part 3 (cushion-seat) and part 5 (backrest-seat)
        // But PART property is 0-indexed from dummy (master is index 0 in positions list)
        // part 3 in positions = PART value 2 in dummy (i-1 where i=3)
        // part 5 in positions = PART value 4 in dummy (i-1 where i=5)
        private val SEAT_PARTS = setOf(2, 4)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0))
    }

    override fun codec(): MapCodec<CatSofaDummyBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, PART)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CatSofaDummyBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val part = state.getValue(PART)
        if (part in SEAT_PARTS) {
            return trySit(level, pos, state, player)
        }
        return InteractionResult.PASS
    }

    private fun trySit(level: Level, pos: BlockPos, state: BlockState, player: Player): InteractionResult {
        if (!player.mainHandItem.isEmpty) return InteractionResult.PASS
        if (player.isShiftKeyDown) return InteractionResult.PASS
        if (player.isPassenger) return InteractionResult.PASS

        // Find cushion block position (part index 2 in dummy = position index 3)
        val be = level.getBlockEntity(pos)
        if (be !is CatSofaDummyBlockEntity) return InteractionResult.PASS

        val masterPos = be.masterPos
        val masterState = level.getBlockState(masterPos)
        if (masterState.block !is CatSofaBlock) return InteractionResult.PASS

        val facing = masterState.getValue(CatSofaBlock.FACING)
        val masterBlock = masterState.block as CatSofaBlock
        val positions = masterBlock.getMultiblockPositions(masterPos, facing)

        // Seat position = cushion block (index 3 = above master)
        val seatPos = positions[3]

        // Check no existing seat
        val area = AABB(seatPos).inflate(0.0, 0.5, 0.0)
        if (level.getEntitiesOfClass(SeatEntity::class.java, area).isNotEmpty()) {
            return InteractionResult.PASS
        }

        val seat = SeatEntity(EstherServerMod.SEAT_ENTITY.get(), level)
        seat.setPos(seatPos.x + 0.5, seatPos.y + 0.3, seatPos.z + 0.5)

        if (!level.addFreshEntity(seat)) return InteractionResult.PASS
        if (!player.startRiding(seat)) {
            seat.discard()
            return InteractionResult.PASS
        }

        return InteractionResult.SUCCESS
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos)
            if (be is CatSofaDummyBlockEntity) {
                val masterPos = be.masterPos
                val masterState = level.getBlockState(masterPos)
                if (masterState.block is CatSofaBlock) {
                    level.destroyBlock(masterPos, false)
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }
}
