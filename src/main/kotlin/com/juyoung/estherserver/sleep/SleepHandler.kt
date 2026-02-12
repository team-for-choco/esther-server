package com.juyoung.estherserver.sleep

import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent
import net.minecraft.server.level.ServerLevel

object SleepHandler {

    @SubscribeEvent
    fun onPlayerSleep(event: CanPlayerSleepEvent) {
        val problem = event.problem
        if (problem != null) return

        val player = event.entity
        val level = player.level()
        if (level !is ServerLevel) return

        val server = level.server
        val allPlayers = server.playerList.players
        val totalPlayers = allPlayers.count { !it.isSpectator }
        val sleepingPlayers = allPlayers.count { it.isSleeping } + 1 // +1 for the player about to sleep

        server.playerList.broadcastSystemMessage(
            Component.translatable(
                "message.estherserver.player_sleeping",
                player.displayName,
                sleepingPlayers.toString(),
                totalPlayers.toString()
            ),
            false
        )
    }

    @SubscribeEvent
    fun onSleepFinished(event: SleepFinishedTimeEvent) {
        val level = event.level
        if (level !is ServerLevel) return

        level.server.playerList.broadcastSystemMessage(
            Component.translatable("message.estherserver.good_morning"),
            false
        )
    }
}
