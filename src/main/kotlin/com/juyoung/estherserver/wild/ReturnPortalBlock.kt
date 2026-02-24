package com.juyoung.estherserver.wild

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ReturnPortalBlock(properties: Properties) : Block(properties) {
    companion object {
        val CODEC: MapCodec<ReturnPortalBlock> = simpleCodec(::ReturnPortalBlock)
    }

    override fun codec(): MapCodec<out Block> = CODEC

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.FAIL

        // 야생 차원에서만 사용 가능
        if (serverPlayer.level().dimension() != WildDimensionKeys.WILD_LEVEL) {
            serverPlayer.displayClientMessage(
                Component.translatable("message.estherserver.return_portal_wrong_dimension"), true
            )
            return InteractionResult.FAIL
        }

        WildTeleportHelper.teleportToOverworld(serverPlayer)

        return InteractionResult.SUCCESS
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
}
