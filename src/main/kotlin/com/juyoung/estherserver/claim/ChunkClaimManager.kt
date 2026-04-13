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

        // 기존 클레임이 있으면 권한을 상속
        val permissions = data.claims.values.find { it.ownerUUID == player.uuid }?.permissions ?: ClaimPermissions()

        data.setClaim(chunkPos, ChunkClaimEntry(
            ownerUUID = player.uuid,
            ownerName = player.gameProfile.name,
            claimedAt = player.serverLevel().gameTime,
            permissions = permissions
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
        return claim.ownerUUID == playerUUID || data.isTrusted(claim.ownerUUID, playerUUID)
    }

    enum class TrustResult { SUCCESS, NO_CLAIMS, ALREADY_TRUSTED, CANNOT_TRUST_SELF }
    enum class UntrustResult { SUCCESS, NO_CLAIMS, NOT_TRUSTED }

    fun addTrust(player: ServerPlayer, targetUUID: UUID, targetName: String): TrustResult {
        val data = ChunkClaimData.get(player.serverLevel())
        if (data.getClaimsByOwner(player.uuid).isEmpty()) return TrustResult.NO_CLAIMS
        if (targetUUID == player.uuid) return TrustResult.CANNOT_TRUST_SELF
        if (data.isTrusted(player.uuid, targetUUID)) return TrustResult.ALREADY_TRUSTED

        data.addTrust(player.uuid, targetUUID, targetName)
        return TrustResult.SUCCESS
    }

    fun removeTrust(player: ServerPlayer, targetUUID: UUID): UntrustResult {
        val data = ChunkClaimData.get(player.serverLevel())
        if (data.getClaimsByOwner(player.uuid).isEmpty()) return UntrustResult.NO_CLAIMS
        if (!data.isTrusted(player.uuid, targetUUID)) return UntrustResult.NOT_TRUSTED

        data.removeTrust(player.uuid, targetUUID)
        return UntrustResult.SUCCESS
    }

    fun getTrustedPlayers(level: ServerLevel, ownerUUID: UUID): Set<UUID> {
        return ChunkClaimData.get(level).getTrustedPlayers(ownerUUID)
    }

    fun isTrusted(level: ServerLevel, ownerUUID: UUID, playerUUID: UUID): Boolean {
        return ChunkClaimData.get(level).isTrusted(ownerUUID, playerUUID)
    }

    fun getTrustedPlayerName(level: ServerLevel, playerUUID: UUID): String {
        return ChunkClaimData.get(level).getTrustedPlayerName(playerUUID)
    }

    enum class UpdateAllPermResult {
        SUCCESS,
        NO_CLAIMS
    }

    fun updateAllPermissions(player: ServerPlayer, type: String, allow: Boolean): Pair<UpdateAllPermResult, Int> {
        val data = ChunkClaimData.get(player.serverLevel())
        val claims = data.getClaimsByOwner(player.uuid)
        if (claims.isEmpty()) return Pair(UpdateAllPermResult.NO_CLAIMS, 0)

        val updatePerms: (ClaimPermissions) -> ClaimPermissions = when (type) {
            "break" -> { p -> p.copy(allowBreak = allow) }
            "place" -> { p -> p.copy(allowPlace = allow) }
            "interact" -> { p -> p.copy(allowInteract = allow) }
            else -> { p -> p }
        }
        for ((chunkPos, existing) in claims) {
            data.setClaim(chunkPos, existing.copy(permissions = updatePerms(existing.permissions)))
        }
        return Pair(UpdateAllPermResult.SUCCESS, claims.size)
    }

    fun getClaimInfo(level: ServerLevel, chunkPos: ChunkPos): ChunkClaimEntry? {
        return ChunkClaimData.get(level).getClaim(chunkPos)
    }

    fun getPlayerClaims(level: ServerLevel, playerUUID: UUID): List<Pair<ChunkPos, ChunkClaimEntry>> {
        return ChunkClaimData.get(level).getClaimsByOwner(playerUUID)
    }
}
