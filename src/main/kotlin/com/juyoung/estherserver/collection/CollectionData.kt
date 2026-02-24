package com.juyoung.estherserver.collection

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

data class CollectionKey(
    val item: ResourceLocation
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putString("item", item.toString())
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): CollectionKey {
            val item = ResourceLocation.parse(tag.getString("item"))
            // Migration: ignore quality field if present in old data
            return CollectionKey(item)
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CollectionKey> = object : StreamCodec<FriendlyByteBuf, CollectionKey> {
            override fun decode(buf: FriendlyByteBuf): CollectionKey {
                val item = buf.readResourceLocation()
                return CollectionKey(item)
            }

            override fun encode(buf: FriendlyByteBuf, value: CollectionKey) {
                buf.writeResourceLocation(value.item)
            }
        }
    }
}

data class CollectionEntry(
    val firstDiscoveredAt: Long,
    val count: Int
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putLong("firstDiscoveredAt", firstDiscoveredAt)
        tag.putInt("count", count)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): CollectionEntry {
            return CollectionEntry(
                firstDiscoveredAt = tag.getLong("firstDiscoveredAt"),
                count = tag.getInt("count")
            )
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CollectionEntry> = object : StreamCodec<FriendlyByteBuf, CollectionEntry> {
            override fun decode(buf: FriendlyByteBuf): CollectionEntry {
                return CollectionEntry(
                    firstDiscoveredAt = buf.readLong(),
                    count = buf.readInt()
                )
            }

            override fun encode(buf: FriendlyByteBuf, value: CollectionEntry) {
                buf.writeLong(value.firstDiscoveredAt)
                buf.writeInt(value.count)
            }
        }
    }
}

class CollectionData(
    private val entries: MutableMap<CollectionKey, CollectionEntry> = mutableMapOf(),
    val unlockedMilestones: MutableSet<String> = mutableSetOf(),
    var activeTitle: String? = null
) {
    fun isComplete(key: CollectionKey): Boolean {
        val entry = entries[key] ?: return false
        return entry.count >= CollectibleRegistry.getRequiredCount(key)
    }

    fun getEntry(key: CollectionKey): CollectionEntry? = entries[key]

    fun getAllEntries(): Map<CollectionKey, CollectionEntry> = entries.toMap()

    fun getCompletedCount(): Int = entries.count { (key, entry) ->
        entry.count >= CollectibleRegistry.getRequiredCount(key)
    }

    fun register(key: CollectionKey, gameTick: Long): Boolean {
        val requiredCount = CollectibleRegistry.getRequiredCount(key)
        val existing = entries[key]
        if (existing != null && existing.count >= requiredCount) {
            return false
        }
        if (existing != null) {
            entries[key] = existing.copy(count = existing.count + 1)
        } else {
            entries[key] = CollectionEntry(firstDiscoveredAt = gameTick, count = 1)
        }
        return true
    }

    fun updateEntry(key: CollectionKey, entry: CollectionEntry) {
        entries[key] = entry
    }

    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        val list = ListTag()
        for ((key, entry) in entries) {
            val entryTag = CompoundTag()
            entryTag.put("key", key.toNBT())
            entryTag.put("entry", entry.toNBT())
            list.add(entryTag)
        }
        tag.put("entries", list)

        val milestoneList = ListTag()
        for (ms in unlockedMilestones) {
            milestoneList.add(StringTag.valueOf(ms))
        }
        tag.put("milestones", milestoneList)

        if (activeTitle != null) {
            tag.putString("activeTitle", activeTitle!!)
        }

        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): CollectionData {
            val data = CollectionData()
            if (tag.contains("entries")) {
                val list = tag.getList("entries", 10)
                // Migration: merge entries with same item (old quality-based data)
                for (i in 0 until list.size) {
                    val entryTag = list.getCompound(i)
                    val keyTag = entryTag.getCompound("key")
                    val item = ResourceLocation.parse(keyTag.getString("item"))
                    val key = CollectionKey(item)
                    val entry = CollectionEntry.fromNBT(entryTag.getCompound("entry"))

                    val existing = data.entries[key]
                    if (existing != null) {
                        // Merge: keep earliest discovery time, sum counts
                        data.entries[key] = CollectionEntry(
                            firstDiscoveredAt = minOf(existing.firstDiscoveredAt, entry.firstDiscoveredAt),
                            count = existing.count + entry.count
                        )
                    } else {
                        data.entries[key] = entry
                    }
                }
            }
            if (tag.contains("milestones")) {
                val milestoneList = tag.getList("milestones", 8) // 8 = StringTag
                for (i in 0 until milestoneList.size) {
                    data.unlockedMilestones.add(milestoneList.getString(i))
                }
            }
            if (tag.contains("activeTitle")) {
                data.activeTitle = tag.getString("activeTitle")
            }
            return data
        }

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CollectionData> = object : StreamCodec<FriendlyByteBuf, CollectionData> {
            override fun decode(buf: FriendlyByteBuf): CollectionData {
                val data = CollectionData()
                val size = buf.readVarInt()
                for (i in 0 until size) {
                    val key = CollectionKey.STREAM_CODEC.decode(buf)
                    val entry = CollectionEntry.STREAM_CODEC.decode(buf)
                    data.entries[key] = entry
                }
                val milestoneCount = buf.readVarInt()
                for (i in 0 until milestoneCount) {
                    data.unlockedMilestones.add(buf.readUtf())
                }
                val hasActiveTitle = buf.readBoolean()
                if (hasActiveTitle) {
                    data.activeTitle = buf.readUtf()
                }
                return data
            }

            override fun encode(buf: FriendlyByteBuf, value: CollectionData) {
                buf.writeVarInt(value.entries.size)
                for ((key, entry) in value.entries) {
                    CollectionKey.STREAM_CODEC.encode(buf, key)
                    CollectionEntry.STREAM_CODEC.encode(buf, entry)
                }
                buf.writeVarInt(value.unlockedMilestones.size)
                for (ms in value.unlockedMilestones) {
                    buf.writeUtf(ms)
                }
                buf.writeBoolean(value.activeTitle != null)
                if (value.activeTitle != null) {
                    buf.writeUtf(value.activeTitle!!)
                }
            }
        }
    }
}
