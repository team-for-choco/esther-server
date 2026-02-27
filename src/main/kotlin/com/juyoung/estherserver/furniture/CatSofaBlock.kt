package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.sitting.SeatEntity
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
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
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import java.util.function.Supplier

open class CatSofaBlock(
    properties: Properties,
    private val dummyBlockSupplier: Supplier<Block> = Supplier { EstherServerMod.CAT_SOFA_DUMMY.get() },
    private val noSpaceMessageKey: String = "message.estherserver.cat_sofa_no_space"
) : BaseEntityBlock(properties) {

    companion object {
        val FACING = HorizontalDirectionalBlock.FACING
        val CODEC: MapCodec<CatSofaBlock> = simpleCodec(::CatSofaBlock)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out CatSofaBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = null

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (level.isClientSide) return

        val facing = state.getValue(FACING)
        val positions = getMultiblockPositions(pos, facing)

        // Check all positions are air (skip master at index 0)
        for (i in 1 until positions.size) {
            if (!level.getBlockState(positions[i]).isAir) {
                level.destroyBlock(pos, true)
                if (placer is ServerPlayer) {
                    placer.displayClientMessage(
                        Component.translatable(noSpaceMessageKey), true
                    )
                }
                return
            }
        }

        // Place dummy blocks
        val dummyBlock = dummyBlockSupplier.get()
        for (i in 1 until positions.size) {
            val dummyState = dummyBlock.defaultBlockState()
                .setValue(CatSofaDummyBlock.FACING, facing)
                .setValue(CatSofaDummyBlock.PART, i - 1)
            level.setBlock(positions[i], dummyState, 3)
            val be = level.getBlockEntity(positions[i])
            if (be is AbstractSofaDummyBlockEntity) {
                be.setMasterPos(pos)
            }
        }
    }

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hitResult: BlockHitResult
    ): InteractionResult {
        // Master block is frame (bottom layer) — no sitting here
        return InteractionResult.PASS
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val facing = state.getValue(FACING)
            val positions = getMultiblockPositions(pos, facing)
            for (i in 1 until positions.size) {
                val blockState = level.getBlockState(positions[i])
                if (blockState.block is CatSofaDummyBlock) {
                    // Remove any seat entities at upper blocks
                    removeSeatAt(level, positions[i])
                    level.destroyBlock(positions[i], false)
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }

    /**
     * Returns 8 positions for the 2x2x2 multiblock.
     * Index 0 = master (front-seat-bottom).
     *
     * Model convention: front=South, cat side=East (+X).
     * catSide = counterClockWise (facing=SOUTH → EAST, facing=NORTH → WEST)
     */
    fun getMultiblockPositions(masterPos: BlockPos, facing: Direction): List<BlockPos> {
        val catSide = facing.counterClockWise
        val back = facing.opposite
        return listOf(
            masterPos,                                                          // master: frame-front-seat
            masterPos.relative(catSide),                                          // part 0: frame-front-cat
            masterPos.relative(back),                                           // part 1: frame-back-seat
            masterPos.relative(catSide).relative(back),                           // part 2: frame-back-cat
            masterPos.above(),                                                  // part 3: cushion-seat (sit)
            masterPos.relative(catSide).above(),                                  // part 4: cat-front
            masterPos.relative(back).above(),                                   // part 5: backrest-seat (sit)
            masterPos.relative(catSide).relative(back).above()                    // part 6: cat-back+backrest
        )
    }

    private fun removeSeatAt(level: Level, pos: BlockPos) {
        val area = AABB(pos).inflate(0.0, 0.5, 0.0)
        level.getEntitiesOfClass(SeatEntity::class.java, area).forEach { it.discard() }
    }
}
