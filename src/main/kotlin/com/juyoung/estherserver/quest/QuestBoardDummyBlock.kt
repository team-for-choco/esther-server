package com.juyoung.estherserver.quest

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
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
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult

class QuestBoardDummyBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val PART = IntegerProperty.create("part", 0, 8)
        val CODEC: MapCodec<QuestBoardDummyBlock> = simpleCodec(::QuestBoardDummyBlock)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0))
    }

    override fun codec(): MapCodec<QuestBoardDummyBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, PART)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return QuestBoardDummyBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        return delegateToMaster(level, pos, serverPlayer, InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND))
    }

    override fun useItemOn(
        stack: ItemStack, state: BlockState, level: Level, pos: BlockPos,
        player: Player, hand: InteractionHand, hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        return delegateToMaster(level, pos, serverPlayer, hand, stack)
    }

    private fun delegateToMaster(level: Level, pos: BlockPos, player: ServerPlayer, hand: InteractionHand, stack: ItemStack): InteractionResult {
        val be = level.getBlockEntity(pos)
        if (be is QuestBoardDummyBlockEntity) {
            val masterPos = be.masterPos
            val masterState = level.getBlockState(masterPos)
            if (masterState.block is QuestBoardBlock) {
                return QuestHandler.handleBoardInteraction(player, hand, stack)
            }
        }
        return InteractionResult.PASS
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos)
            if (be is QuestBoardDummyBlockEntity) {
                val masterPos = be.masterPos
                val masterState = level.getBlockState(masterPos)
                if (masterState.block is QuestBoardBlock) {
                    val masterBlock = masterState.block as QuestBoardBlock
                    val facing = masterState.getValue(QuestBoardBlock.FACING)
                    val right = facing.clockWise
                    val positions = masterBlock.getMultiblockPositions(masterPos, right)

                    // Remove all other dummy blocks
                    for (i in 1 until positions.size) {
                        if (positions[i] != pos) {
                            val blockState = level.getBlockState(positions[i])
                            if (blockState.block is QuestBoardDummyBlock) {
                                level.destroyBlock(positions[i], false)
                            }
                        }
                    }

                    // Drop item if not creative
                    if (!player.isCreative) {
                        Block.popResource(level, masterPos, ItemStack(EstherServerMod.QUEST_BOARD_ITEM.get()))
                    }

                    // Remove master block
                    level.destroyBlock(masterPos, false)
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }
}
