package com.juyoung.estherserver.wild

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import java.util.EnumSet
import net.minecraft.world.entity.Relative
import net.minecraft.world.level.storage.LevelResource

object WildCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("wild")
                .then(
                    Commands.literal("reset")
                        .requires { it.hasPermission(2) }
                        .executes { context -> resetWild(context.source) }
                )
        )
    }

    private fun resetWild(source: CommandSourceStack): Int {
        val server = source.server
        val wildLevel = server.getLevel(WildDimensionKeys.WILD_LEVEL)

        if (wildLevel == null) {
            source.sendFailure(Component.translatable("message.estherserver.wild_reset_error"))
            return 0
        }

        // 야생 차원에 있는 플레이어를 모두 오버월드로 대피
        val overworld = server.getLevel(Level.OVERWORLD) ?: return 0
        val playersInWild = wildLevel.players().toList()
        var evacuatedCount = 0

        for (player in playersInWild) {
            if (player is ServerPlayer) {
                val returnData = player.getData(ModWild.RETURN_DATA.get())

                if (returnData.hasData) {
                    player.teleportTo(
                        overworld,
                        returnData.x,
                        returnData.y,
                        returnData.z,
                        EnumSet.noneOf(Relative::class.java),
                        returnData.yaw,
                        returnData.pitch,
                        false
                    )
                } else {
                    val spawn = overworld.sharedSpawnPos
                    player.teleportTo(
                        overworld,
                        spawn.x.toDouble() + 0.5,
                        spawn.y.toDouble(),
                        spawn.z.toDouble() + 0.5,
                        EnumSet.noneOf(Relative::class.java),
                        player.yRot,
                        player.xRot,
                        false
                    )
                }
                evacuatedCount++
            }
        }

        // 고정 스폰 위치 초기화 (다음 진입 시 새 위치 생성)
        WildSpawnData.get(server).clearSpawn()

        // 야생 차원 자동 저장 비활성화 후 파일 삭제
        try {
            wildLevel.noSave = true

            val worldDir = server.getWorldPath(LevelResource.ROOT)
            val wildDimDir = worldDir.resolve("dimensions").resolve("estherserver").resolve("wild").toFile()

            if (wildDimDir.exists()) {
                wildDimDir.deleteRecursively()
            }
        } catch (e: Exception) {
            source.sendFailure(Component.translatable("message.estherserver.wild_reset_error"))
            return 0
        }

        // 서버 전체 브로드캐스트
        server.playerList.broadcastSystemMessage(
            Component.translatable("message.estherserver.wild_reset_broadcast"), false
        )

        source.sendSuccess({
            Component.translatable("message.estherserver.wild_reset_success", evacuatedCount)
        }, true)

        return 1
    }
}
