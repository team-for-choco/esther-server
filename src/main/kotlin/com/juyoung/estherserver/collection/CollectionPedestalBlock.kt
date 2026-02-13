package com.juyoung.estherserver.collection

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
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
        }

        return InteractionResult.SUCCESS
    }
}
