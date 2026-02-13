package com.juyoung.estherserver.claim

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.BlockEvent

object ClaimProtectionHandler {

    private const val OP_LEVEL = 2
    const val FAKE_PLAYER_NAME = "FakePlayer"

    private fun canBypassProtection(player: ServerPlayer, level: ServerLevel, chunkPos: ChunkPos): Boolean {
        if (!player.hasPermissions(OP_LEVEL)) return false
        val claim = ChunkClaimManager.getClaimInfo(level, chunkPos) ?: return true
        // fakeclaim은 OP도 우회 불가 (테스트용)
        return claim.ownerName != FAKE_PLAYER_NAME
    }

    @SubscribeEvent
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        val player = event.player
        val level = player.level()
        if (level.isClientSide) return
        val serverLevel = level as? ServerLevel ?: return
        val serverPlayer = player as? ServerPlayer ?: return

        val chunkPos = ChunkPos(event.pos)
        if (canBypassProtection(serverPlayer, serverLevel, chunkPos)) return

        if (!ChunkClaimManager.canModify(serverLevel, chunkPos, player.uuid)) {
            event.isCanceled = true
            serverPlayer.displayClientMessage(
                Component.translatable("message.estherserver.claim_protected_break"), true
            )
        }
    }

    @SubscribeEvent
    fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
        val entity = event.entity ?: return
        val level = entity.level()
        if (level.isClientSide) return
        val serverLevel = level as? ServerLevel ?: return
        val serverPlayer = entity as? ServerPlayer ?: return

        val chunkPos = ChunkPos(event.pos)
        if (canBypassProtection(serverPlayer, serverLevel, chunkPos)) return

        if (!ChunkClaimManager.canModify(serverLevel, chunkPos, serverPlayer.uuid)) {
            event.isCanceled = true
            serverPlayer.inventoryMenu.sendAllDataToRemote()
            serverPlayer.displayClientMessage(
                Component.translatable("message.estherserver.claim_protected_place"), true
            )
        }
    }
}
