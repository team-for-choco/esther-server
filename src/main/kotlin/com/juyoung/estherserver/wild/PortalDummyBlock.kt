package com.juyoung.estherserver.wild

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult

class PortalDummyBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val PART: IntegerProperty = IntegerProperty.create("part", 0, 1)
        val CODEC: MapCodec<PortalDummyBlock> = simpleCodec(::PortalDummyBlock)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(PART, 0))
    }

    override fun codec(): MapCodec<out PortalDummyBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(PART)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return PortalDummyBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        // master 블록의 handleInteraction에 위임
        val be = level.getBlockEntity(pos)
        if (be is PortalDummyBlockEntity) {
            val masterPos = be.masterPos
            val masterBlock = level.getBlockState(masterPos).block
            if (masterBlock is AbstractPortalBlock) {
                return masterBlock.handleInteraction(level, player)
            }
        }
        return InteractionResult.PASS
    }

    override fun useItemOn(
        stack: net.minecraft.world.item.ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: net.minecraft.world.InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        return InteractionResult.TRY_WITH_EMPTY_HAND
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player): BlockState {
        if (!level.isClientSide) {
            val be = level.getBlockEntity(pos)
            if (be is PortalDummyBlockEntity) {
                val masterPos = be.masterPos
                // 형제 더미 블록 직접 제거 (destroyBlock은 playerWillDestroy를 호출하지 않으므로)
                removeSiblings(level, pos, masterPos)
                // master 제거
                if (level.getBlockState(masterPos).block is AbstractPortalBlock) {
                    level.destroyBlock(masterPos, false)
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }

    private fun removeSiblings(level: Level, currentPos: BlockPos, masterPos: BlockPos) {
        val above1 = masterPos.above()
        val above2 = masterPos.above(2)
        if (above1 != currentPos && level.getBlockState(above1).block is PortalDummyBlock) {
            level.destroyBlock(above1, false)
        }
        if (above2 != currentPos && level.getBlockState(above2).block is PortalDummyBlock) {
            level.destroyBlock(above2, false)
        }
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        AbstractPortalBlock.spawnPortalParticles(level, pos, random)
    }
}
