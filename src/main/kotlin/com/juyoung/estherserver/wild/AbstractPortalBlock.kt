package com.juyoung.estherserver.wild

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

abstract class AbstractPortalBlock(properties: Properties) : Block(properties) {

    companion object {
        fun spawnPortalParticles(level: Level, pos: BlockPos, random: RandomSource) {
            for (i in 0..2) {
                val x = pos.x + 0.5 + (random.nextDouble() - 0.5) * 0.8
                val y = pos.y + random.nextDouble() * 1.0
                val z = pos.z + 0.5 + (random.nextDouble() - 0.5) * 0.8
                level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0, 0.5, 0.0)
            }
        }
    }

    abstract fun getDummyBlock(): Block
    abstract fun performTeleport(player: ServerPlayer): Boolean
    abstract fun getWrongDimensionMessageKey(): String

    /** PortalDummyBlock에서 호출 가능한 public 텔레포트 핸들러 */
    fun handleInteraction(level: Level, player: Player): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.FAIL
        if (!isCorrectDimension(serverPlayer)) {
            serverPlayer.displayClientMessage(
                Component.translatable(getWrongDimensionMessageKey()), true
            )
            return InteractionResult.FAIL
        }
        if (!performTeleport(serverPlayer)) {
            serverPlayer.displayClientMessage(
                Component.translatable("message.estherserver.wild_no_safe_location"), true
            )
        }
        return InteractionResult.SUCCESS
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (level.isClientSide) return

        // 위 2칸이 공기인지 확인
        val above1 = pos.above()
        val above2 = pos.above(2)
        if (!level.getBlockState(above1).isAir || !level.getBlockState(above2).isAir) {
            level.destroyBlock(pos, true)
            if (placer is ServerPlayer) {
                placer.displayClientMessage(
                    Component.translatable("message.estherserver.portal_no_space"), true
                )
            }
            return
        }

        val dummyBlock = getDummyBlock()
        // Middle (part 0)
        level.setBlock(above1, dummyBlock.defaultBlockState().setValue(PortalDummyBlock.PART, 0), 3)
        (level.getBlockEntity(above1) as? PortalDummyBlockEntity)?.setMasterPos(pos)
        // Top (part 1)
        level.setBlock(above2, dummyBlock.defaultBlockState().setValue(PortalDummyBlock.PART, 1), 3)
        (level.getBlockEntity(above2) as? PortalDummyBlockEntity)?.setMasterPos(pos)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        return handleInteraction(level, player)
    }

    override fun useItemOn(
        stack: ItemStack,
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
            // 더미 블록 연쇄 제거
            val above1 = pos.above()
            val above2 = pos.above(2)
            if (level.getBlockState(above1).block is PortalDummyBlock) {
                level.destroyBlock(above1, false)
            }
            if (level.getBlockState(above2).block is PortalDummyBlock) {
                level.destroyBlock(above2, false)
            }
        }
        return super.playerWillDestroy(level, pos, state, player)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        spawnPortalParticles(level, pos, random)
    }

    internal abstract fun isCorrectDimension(player: ServerPlayer): Boolean
}
