package com.juyoung.estherserver.collection

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

object ChatTitleHandler {

    private const val TEAM_PREFIX = "esther_t_"

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        applyTitleTeam(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        applyTitleTeam(player)
    }

    fun applyTitleTeam(player: ServerPlayer) {
        val scoreboard = player.server.scoreboard
        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        val activeTitleId = data.activeTitle
        val playerName = player.scoreboardName

        // 기존 칭호 팀에서 제거
        val currentTeam = scoreboard.getPlayersTeam(playerName)
        if (currentTeam != null && currentTeam.name.startsWith(TEAM_PREFIX)) {
            scoreboard.removePlayerFromTeam(playerName, currentTeam)
            // 팀에 아무도 없으면 삭제
            if (currentTeam.players.isEmpty()) {
                scoreboard.removePlayerTeam(currentTeam)
            }
        }

        // 칭호가 없으면 여기서 종료
        if (activeTitleId == null) return
        val milestone = Milestone.byId(activeTitleId) ?: return

        // 팀 가져오기 또는 생성
        val teamName = TEAM_PREFIX + milestone.id
        val team = scoreboard.getPlayerTeam(teamName)
            ?: scoreboard.addPlayerTeam(teamName)

        // 팀 prefix 설정
        team.playerPrefix = Component.literal("[")
            .append(Component.translatable(milestone.titleKey).withStyle(milestone.color))
            .append(Component.literal("] "))
        team.color = ChatFormatting.RESET

        // 플레이어를 팀에 추가
        scoreboard.addPlayerToTeam(playerName, team)
    }
}
