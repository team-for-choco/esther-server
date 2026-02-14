package com.juyoung.estherserver.claim

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import java.util.UUID

object ChunkClaimManager {

    sealed class ClaimResult {
        data object SUCCESS : ClaimResult()
        data object ALREADY_OWNED_BY_SELF : ClaimResult()
        data class OWNED_BY_OTHER(val ownerName: String) : ClaimResult()
    }

    fun claim(player: ServerPlayer, chunkPos: ChunkPos): ClaimResult {
        val data = ChunkClaimData.get(player.serverLevel())
        val existing = data.getClaim(chunkPos)

        if (existing != null) {
            return if (existing.ownerUUID == player.uuid) {
                ClaimResult.ALREADY_OWNED_BY_SELF
            } else {
                ClaimResult.OWNED_BY_OTHER(existing.ownerName)
            }
        }

        data.setClaim(chunkPos, ChunkClaimEntry(
            ownerUUID = player.uuid,
            ownerName = player.gameProfile.name,
            claimedAt = player.serverLevel().gameTime
        ))
        return ClaimResult.SUCCESS
    }

    enum class UnclaimResult {
        SUCCESS,
        NOT_CLAIMED,
        NOT_OWNER
    }

    fun unclaim(player: ServerPlayer, chunkPos: ChunkPos): UnclaimResult {
        val data = ChunkClaimData.get(player.serverLevel())
        val existing = data.getClaim(chunkPos) ?: return UnclaimResult.NOT_CLAIMED

        if (existing.ownerUUID != player.uuid) {
            return UnclaimResult.NOT_OWNER
        }

        data.removeClaim(chunkPos)
        return UnclaimResult.SUCCESS
    }

    fun canModify(level: ServerLevel, chunkPos: ChunkPos, playerUUID: UUID): Boolean {
        val data = ChunkClaimData.get(level)
        val claim = data.getClaim(chunkPos) ?: return true
        return claim.ownerUUID == playerUUID
    }

    enum class UpdatePermResult {
        SUCCESS,
        NOT_CLAIMED,
        NOT_OWNER
    }

    fun updatePermissions(player: ServerPlayer, chunkPos: ChunkPos, permissions: ClaimPermissions): UpdatePermResult {
        val data = ChunkClaimData.get(player.serverLevel())
        val existing = data.getClaim(chunkPos) ?: return UpdatePermResult.NOT_CLAIMED

        if (existing.ownerUUID != player.uuid) {
            return UpdatePermResult.NOT_OWNER
        }

        data.setClaim(chunkPos, existing.copy(permissions = permissions))
        return UpdatePermResult.SUCCESS
    }

    fun getClaimInfo(level: ServerLevel, chunkPos: ChunkPos): ChunkClaimEntry? {
        return ChunkClaimData.get(level).getClaim(chunkPos)
    }

    fun getPlayerClaims(level: ServerLevel, playerUUID: UUID): List<Pair<ChunkPos, ChunkClaimEntry>> {
        return ChunkClaimData.get(level).getClaimsByOwner(playerUUID)
    }
}
