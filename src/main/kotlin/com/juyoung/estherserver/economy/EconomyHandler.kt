package com.juyoung.estherserver.economy

import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object EconomyHandler {

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModEconomy.BALANCE_DATA.get())
        PacketDistributor.sendToPlayer(player, BalanceSyncPayload(data.balance))
    }

    fun getBalance(player: ServerPlayer): Long {
        return player.getData(ModEconomy.BALANCE_DATA.get()).balance
    }

    fun addBalance(player: ServerPlayer, amount: Long) {
        val data = player.getData(ModEconomy.BALANCE_DATA.get())
        data.balance += amount
        player.setData(ModEconomy.BALANCE_DATA.get(), data)
        syncToClient(player)
    }

    fun removeBalance(player: ServerPlayer, amount: Long): Boolean {
        val data = player.getData(ModEconomy.BALANCE_DATA.get())
        if (data.balance < amount) return false
        data.balance -= amount
        player.setData(ModEconomy.BALANCE_DATA.get(), data)
        syncToClient(player)
        return true
    }

    fun setBalance(player: ServerPlayer, amount: Long) {
        val data = player.getData(ModEconomy.BALANCE_DATA.get())
        data.balance = amount
        player.setData(ModEconomy.BALANCE_DATA.get(), data)
        syncToClient(player)
    }
}
