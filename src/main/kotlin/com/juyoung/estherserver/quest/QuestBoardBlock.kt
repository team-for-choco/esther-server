package com.juyoung.estherserver.quest

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

class QuestBoardBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val CODEC: MapCodec<QuestBoardBlock> = simpleCodec(::QuestBoardBlock)
        // Multiblock: 4 wide x 3 tall (master at bottom-left)
        const val WIDTH = 4
        const val HEIGHT = 3
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<QuestBoardBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return QuestBoardBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    /**
     * On placement, validate and place dummy blocks for the 4x3 multiblock.
     */
    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (level.isClientSide) return

        val facing = state.getValue(FACING)
        val right = facing.clockWise  // right direction relative to facing
        val positions = getMultiblockPositions(pos, right)

        // Check all positions are air (skip master pos at index 0)
        for (i in 1 until positions.size) {
            if (!level.getBlockState(positions[i]).isAir) {
                // Cannot place — remove master block
                level.destroyBlock(pos, true)
                if (placer is ServerPlayer) {
                    placer.displayClientMessage(
                        Component.translatable("message.estherserver.quest_board_no_space"), true
                    )
                }
                return
            }
        }

        // Place dummy blocks with part-specific state
        for (i in 1 until positions.size) {
            val dummyState = EstherServerMod.QUEST_BOARD_DUMMY.get().defaultBlockState()
                .setValue(QuestBoardDummyBlock.FACING, facing)
                .setValue(QuestBoardDummyBlock.PART, i - 1)
            level.setBlock(positions[i], dummyState, 3)
            val be = level.getBlockEntity(positions[i])
            if (be is QuestBoardDummyBlockEntity) {
                be.setMasterPos(pos)
            }
        }
    }

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        return QuestHandler.handleBoardInteraction(serverPlayer, InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND))
    }

    override fun useItemOn(
        stack: ItemStack, state: BlockState, level: Level, pos: BlockPos,
        player: Player, hand: InteractionHand, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        return QuestHandler.handleBoardInteraction(serverPlayer, hand, stack)
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val facing = state.getValue(FACING)
            val right = facing.clockWise
            val positions = getMultiblockPositions(pos, right)
            for (i in 1 until positions.size) {
                val blockState = level.getBlockState(positions[i])
                if (blockState.block is QuestBoardDummyBlock) {
                    level.destroyBlock(positions[i], false)
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }

    /**
     * Returns 10 positions (4 wide x 3 tall, skipping bottom-middle 2) for the multiblock.
     * Index 0 is the master position (bottom-left leg).
     * Bottom-middle positions (dx=1,2 dy=0) are skipped (empty space under the board).
     */
    fun getMultiblockPositions(masterPos: BlockPos, right: Direction): List<BlockPos> {
        val positions = mutableListOf<BlockPos>()
        for (dy in 0 until HEIGHT) {
            for (dx in 0 until WIDTH) {
                if (dy == 0 && (dx == 1 || dx == 2)) continue
                positions.add(masterPos.relative(right, dx).above(dy))
            }
        }
        return positions
    }
}
