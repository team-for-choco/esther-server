package com.juyoung.estherserver.collection

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class CollectionSyncPayload(val data: CollectionData) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<CollectionSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "collection_sync")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CollectionSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, CollectionSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): CollectionSyncPayload {
                    return CollectionSyncPayload(CollectionData.STREAM_CODEC.decode(buf))
                }

                override fun encode(buf: FriendlyByteBuf, value: CollectionSyncPayload) {
                    CollectionData.STREAM_CODEC.encode(buf, value.data)
                }
            }
    }
}

class CollectionUpdatePayload(
    val key: CollectionKey,
    val entry: CollectionEntry,
    val completedCount: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<CollectionUpdatePayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "collection_update")
        )

        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, CollectionUpdatePayload> =
            object : StreamCodec<FriendlyByteBuf, CollectionUpdatePayload> {
                override fun decode(buf: FriendlyByteBuf): CollectionUpdatePayload {
                    val key = CollectionKey.STREAM_CODEC.decode(buf)
                    val entry = CollectionEntry.STREAM_CODEC.decode(buf)
                    val completedCount = buf.readVarInt()
                    return CollectionUpdatePayload(key, entry, completedCount)
                }

                override fun encode(buf: FriendlyByteBuf, value: CollectionUpdatePayload) {
                    CollectionKey.STREAM_CODEC.encode(buf, value.key)
                    CollectionEntry.STREAM_CODEC.encode(buf, value.entry)
                    buf.writeVarInt(value.completedCount)
                }
            }
    }
}
