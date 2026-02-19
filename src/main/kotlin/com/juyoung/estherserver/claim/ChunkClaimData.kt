package com.juyoung.estherserver.claim

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

data class ChunkClaimEntry(
    val ownerUUID: UUID,
    val ownerName: String,
    val claimedAt: Long,
    val yMin: Int = 0,
    val yMax: Int = 319,
    val permissions: ClaimPermissions = ClaimPermissions()
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putUUID("ownerUUID", ownerUUID)
        tag.putString("ownerName", ownerName)
        tag.putLong("claimedAt", claimedAt)
        tag.putInt("yMin", yMin)
        tag.putInt("yMax", yMax)
        tag.put("permissions", permissions.toNBT())
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): ChunkClaimEntry {
            val permissions = if (tag.contains("permissions"))
                ClaimPermissions.fromNBT(tag.getCompound("permissions"))
            else
                ClaimPermissions()

            return ChunkClaimEntry(
                ownerUUID = tag.getUUID("ownerUUID"),
                ownerName = tag.getString("ownerName"),
                claimedAt = tag.getLong("claimedAt"),
                yMin = if (tag.contains("yMin")) tag.getInt("yMin") else 0,
                yMax = if (tag.contains("yMax")) tag.getInt("yMax") else 319,
                permissions = permissions
            )
        }
    }
}

class ChunkClaimData private constructor(
    val claims: MutableMap<Long, ChunkClaimEntry> = mutableMapOf(),
    val trustedPlayers: MutableMap<UUID, MutableSet<UUID>> = mutableMapOf(),
    val trustedPlayerNames: MutableMap<UUID, String> = mutableMapOf()
) : SavedData() {

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val list = ListTag()
        for ((chunkKey, entry) in claims) {
            val entryTag = entry.toNBT()
            entryTag.putLong("chunkKey", chunkKey)
            list.add(entryTag)
        }
        tag.put("claims", list)

        val trustList = ListTag()
        for ((ownerUUID, trustedSet) in trustedPlayers) {
            val ownerTag = CompoundTag()
            ownerTag.putUUID("ownerUUID", ownerUUID)
            val playerList = ListTag()
            for (trustedUUID in trustedSet) {
                val playerTag = CompoundTag()
                playerTag.putUUID("playerUUID", trustedUUID)
                playerList.add(playerTag)
            }
            ownerTag.put("players", playerList)
            trustList.add(ownerTag)
        }
        tag.put("trustedPlayers", trustList)

        val nameCache = ListTag()
        for ((uuid, name) in trustedPlayerNames) {
            val entry = CompoundTag()
            entry.putUUID("playerUUID", uuid)
            entry.putString("playerName", name)
            nameCache.add(entry)
        }
        tag.put("playerNameCache", nameCache)

        return tag
    }

    fun getClaim(chunkPos: ChunkPos): ChunkClaimEntry? = claims[chunkPos.toLong()]

    fun setClaim(chunkPos: ChunkPos, entry: ChunkClaimEntry) {
        claims[chunkPos.toLong()] = entry
        setDirty()
    }

    fun removeClaim(chunkPos: ChunkPos): ChunkClaimEntry? {
        val removed = claims.remove(chunkPos.toLong())
        if (removed != null) setDirty()
        return removed
    }

    fun getClaimsByOwner(ownerUUID: UUID): List<Pair<ChunkPos, ChunkClaimEntry>> {
        return claims.entries
            .filter { it.value.ownerUUID == ownerUUID }
            .map { ChunkPos(it.key) to it.value }
    }

    fun getTrustedPlayers(ownerUUID: UUID): Set<UUID> {
        return trustedPlayers[ownerUUID] ?: emptySet()
    }

    fun addTrust(ownerUUID: UUID, targetUUID: UUID, targetName: String) {
        trustedPlayers.getOrPut(ownerUUID) { mutableSetOf() }.add(targetUUID)
        trustedPlayerNames[targetUUID] = targetName
        setDirty()
    }

    fun removeTrust(ownerUUID: UUID, targetUUID: UUID) {
        trustedPlayers[ownerUUID]?.remove(targetUUID)
        if (trustedPlayers[ownerUUID]?.isEmpty() == true) {
            trustedPlayers.remove(ownerUUID)
        }
        setDirty()
    }

    fun isTrusted(ownerUUID: UUID, playerUUID: UUID): Boolean {
        return trustedPlayers[ownerUUID]?.contains(playerUUID) == true
    }

    fun getTrustedPlayerName(playerUUID: UUID): String {
        return trustedPlayerNames[playerUUID] ?: "???"
    }

    companion object {
        private const val DATA_NAME = "estherserver_chunk_claims"

        private val factory = Factory(
            { ChunkClaimData() },
            { tag, _ -> load(tag) }
        )

        private fun load(tag: CompoundTag): ChunkClaimData {
            val data = ChunkClaimData()
            if (tag.contains("claims")) {
                val list = tag.getList("claims", 10)
                for (i in 0 until list.size) {
                    val entryTag = list.getCompound(i)
                    val chunkKey = entryTag.getLong("chunkKey")
                    val entry = ChunkClaimEntry.fromNBT(entryTag)
                    data.claims[chunkKey] = entry
                }
            }
            if (tag.contains("trustedPlayers")) {
                val trustList = tag.getList("trustedPlayers", 10)
                for (i in 0 until trustList.size) {
                    val ownerTag = trustList.getCompound(i)
                    val ownerUUID = ownerTag.getUUID("ownerUUID")
                    val playerList = ownerTag.getList("players", 10)
                    val trustedSet = mutableSetOf<UUID>()
                    for (j in 0 until playerList.size) {
                        val playerTag = playerList.getCompound(j)
                        val playerUUID = playerTag.getUUID("playerUUID")
                        trustedSet.add(playerUUID)
                        // 하위 호환: 구 포맷에서 이름이 있으면 로드
                        if (playerTag.contains("playerName")) {
                            data.trustedPlayerNames[playerUUID] = playerTag.getString("playerName")
                        }
                    }
                    data.trustedPlayers[ownerUUID] = trustedSet
                }
            }
            // 신규 포맷: 별도 이름 캐시 (구 포맷 값을 덮어씀)
            if (tag.contains("playerNameCache")) {
                val nameCache = tag.getList("playerNameCache", 10)
                for (i in 0 until nameCache.size) {
                    val entry = nameCache.getCompound(i)
                    data.trustedPlayerNames[entry.getUUID("playerUUID")] = entry.getString("playerName")
                }
            }
            return data
        }

        fun get(server: MinecraftServer): ChunkClaimData {
            val overworld = server.getLevel(Level.OVERWORLD)
                ?: throw IllegalStateException("Overworld not loaded")
            return overworld.dataStorage.computeIfAbsent(factory, DATA_NAME)
        }

        fun get(level: ServerLevel): ChunkClaimData = get(level.server)
    }
}
