package com.juyoung.estherserver.claim

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level

class LandDeedItem(properties: Properties) : Item(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        val chunkPos = ChunkPos(serverPlayer.blockPosition())

        val result = ChunkClaimManager.claim(serverPlayer, chunkPos)

        when (result) {
            is ChunkClaimManager.ClaimResult.SUCCESS -> {
                serverPlayer.getItemInHand(hand).shrink(1)
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_success",
                        chunkPos.x.toString(),
                        chunkPos.z.toString()
                    ), false
                )
            }
            is ChunkClaimManager.ClaimResult.ALREADY_OWNED_BY_SELF -> {
                serverPlayer.displayClientMessage(
                    Component.translatable("message.estherserver.claim_already_owned"), true
                )
            }
            is ChunkClaimManager.ClaimResult.OWNED_BY_OTHER -> {
                serverPlayer.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_owned_by_other",
                        result.ownerName
                    ), true
                )
            }
        }

        return InteractionResult.SUCCESS
    }
}
