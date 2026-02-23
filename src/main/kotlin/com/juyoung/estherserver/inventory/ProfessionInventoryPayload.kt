package com.juyoung.estherserver.inventory

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/** Server -> Client: sync profession inventory data */
class ProfessionInventoryPayload {

    class SyncPayload(val data: ProfessionInventoryData) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<SyncPayload>(
                ResourceLocation.fromNamespaceAndPath("estherserver", "prof_inv_sync")
            )

            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SyncPayload> =
                object : StreamCodec<RegistryFriendlyByteBuf, SyncPayload> {
                    override fun decode(buf: RegistryFriendlyByteBuf): SyncPayload {
                        return SyncPayload(ProfessionInventoryData.STREAM_CODEC.decode(buf))
                    }

                    override fun encode(buf: RegistryFriendlyByteBuf, value: SyncPayload) {
                        ProfessionInventoryData.STREAM_CODEC.encode(buf, value.data)
                    }
                }
        }
    }

    /** Client -> Server: request to open profession inventory */
    class OpenPayload : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<OpenPayload>(
                ResourceLocation.fromNamespaceAndPath("estherserver", "prof_inv_open")
            )

            val STREAM_CODEC: StreamCodec<FriendlyByteBuf, OpenPayload> =
                object : StreamCodec<FriendlyByteBuf, OpenPayload> {
                    override fun decode(buf: FriendlyByteBuf): OpenPayload = OpenPayload()
                    override fun encode(buf: FriendlyByteBuf, value: OpenPayload) {}
                }
        }
    }

    /** Client -> Server: move item between slots */
    class MovePayload(
        val fromProfession: Int,
        val fromSlot: Int,
        val toProfession: Int,
        val toSlot: Int
    ) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<MovePayload>(
                ResourceLocation.fromNamespaceAndPath("estherserver", "prof_inv_move")
            )

            val STREAM_CODEC: StreamCodec<FriendlyByteBuf, MovePayload> =
                object : StreamCodec<FriendlyByteBuf, MovePayload> {
                    override fun decode(buf: FriendlyByteBuf): MovePayload {
                        return MovePayload(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt()
                        )
                    }

                    override fun encode(buf: FriendlyByteBuf, value: MovePayload) {
                        buf.writeVarInt(value.fromProfession)
                        buf.writeVarInt(value.fromSlot)
                        buf.writeVarInt(value.toProfession)
                        buf.writeVarInt(value.toSlot)
                    }
                }
        }
    }
}
