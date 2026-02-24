package com.juyoung.estherserver.wild

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData

class WildSpawnData private constructor(
    var spawnX: Int = 0,
    var spawnY: Int = 0,
    var spawnZ: Int = 0,
    var hasSpawn: Boolean = false
) : SavedData() {

    fun getSpawnPos(): BlockPos? {
        if (!hasSpawn) return null
        return BlockPos(spawnX, spawnY, spawnZ)
    }

    fun setSpawnPos(pos: BlockPos) {
        spawnX = pos.x
        spawnY = pos.y
        spawnZ = pos.z
        hasSpawn = true
        setDirty()
    }

    fun clearSpawn() {
        hasSpawn = false
        setDirty()
    }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        tag.putBoolean("hasSpawn", hasSpawn)
        if (hasSpawn) {
            tag.putInt("spawnX", spawnX)
            tag.putInt("spawnY", spawnY)
            tag.putInt("spawnZ", spawnZ)
        }
        return tag
    }

    companion object {
        private const val DATA_NAME = "estherserver_wild_spawn"

        private val factory = Factory(
            { WildSpawnData() },
            { tag, _ -> load(tag) }
        )

        private fun load(tag: CompoundTag): WildSpawnData {
            val hasSpawn = tag.getBoolean("hasSpawn")
            return if (hasSpawn) {
                WildSpawnData(
                    spawnX = tag.getInt("spawnX"),
                    spawnY = tag.getInt("spawnY"),
                    spawnZ = tag.getInt("spawnZ"),
                    hasSpawn = true
                )
            } else {
                WildSpawnData()
            }
        }

        fun get(server: MinecraftServer): WildSpawnData {
            val overworld = server.getLevel(Level.OVERWORLD)
                ?: throw IllegalStateException("Overworld not loaded")
            return overworld.dataStorage.computeIfAbsent(factory, DATA_NAME)
        }
    }
}
