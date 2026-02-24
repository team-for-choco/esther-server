package com.juyoung.estherserver.collection

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class CollectionPedestalBlock(properties: Properties) : Block(properties) {
    companion object {
        val CODEC: MapCodec<CollectionPedestalBlock> = simpleCodec(::CollectionPedestalBlock)
    }

    override fun codec(): MapCodec<out Block> = CODEC

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (stack.isEmpty) return InteractionResult.TRY_WITH_EMPTY_HAND
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.FAIL
        val registered = CollectionHandler.tryRegisterItem(serverPlayer, stack)

        if (registered) {
            stack.shrink(1)
            spawnSuccessEffects(level, pos)
        }

        return InteractionResult.SUCCESS
    }

    private fun spawnSuccessEffects(level: Level, pos: BlockPos) {
        val serverLevel = level as? ServerLevel ?: return

        // 사운드
        level.playSound(
            null, pos,
            SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS,
            1.0f, 1.2f
        )

        // 파티클: enchant + happy_villager
        val cx = pos.x + 0.5
        val cy = pos.y + 1.2
        val cz = pos.z + 0.5
        for (i in 0 until 15) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                cx, cy, cz,
                1,
                0.3, 0.5, 0.3,
                0.1
            )
        }
        for (i in 0 until 8) {
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                cx, cy, cz,
                1,
                0.4, 0.3, 0.4,
                0.0
            )
        }
    }
}
