package com.juyoung.estherserver.inventory

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class ProfessionInventoryPayload {

    /** Server -> Client: sync full profession inventory data (for overview/cached data) */
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

    /** Client -> Server: request to switch tab */
    class TabSwitchPayload(val tabIndex: Int) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<TabSwitchPayload>(
                ResourceLocation.fromNamespaceAndPath("estherserver", "prof_inv_tab_switch")
            )

            val STREAM_CODEC: StreamCodec<FriendlyByteBuf, TabSwitchPayload> =
                object : StreamCodec<FriendlyByteBuf, TabSwitchPayload> {
                    override fun decode(buf: FriendlyByteBuf): TabSwitchPayload {
                        return TabSwitchPayload(buf.readVarInt())
                    }

                    override fun encode(buf: FriendlyByteBuf, value: TabSwitchPayload) {
                        buf.writeVarInt(value.tabIndex)
                    }
                }
        }
    }

    /** Server -> Client: sync tab state (current tab + unlocked slots) */
    class TabSyncPayload(val tab: Int, val unlockedSlots: Int) : CustomPacketPayload {
        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

        companion object {
            val TYPE = CustomPacketPayload.Type<TabSyncPayload>(
                ResourceLocation.fromNamespaceAndPath("estherserver", "prof_inv_tab_sync")
            )

            val STREAM_CODEC: StreamCodec<FriendlyByteBuf, TabSyncPayload> =
                object : StreamCodec<FriendlyByteBuf, TabSyncPayload> {
                    override fun decode(buf: FriendlyByteBuf): TabSyncPayload {
                        return TabSyncPayload(buf.readVarInt(), buf.readVarInt())
                    }

                    override fun encode(buf: FriendlyByteBuf, value: TabSyncPayload) {
                        buf.writeVarInt(value.tab)
                        buf.writeVarInt(value.unlockedSlots)
                    }
                }
        }
    }
}
