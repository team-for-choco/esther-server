package com.juyoung.estherserver.wild

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
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

        val overworld = server.getLevel(Level.OVERWORLD) ?: return 0
        var evacuatedCount = 0

        // 야생 차원에 있는 플레이어를 모두 오버월드로 대피
        evacuatedCount += evacuatePlayers(wildLevel, overworld)

        // 네더에 있는 플레이어 대피
        val netherLevel = server.getLevel(Level.NETHER)
        if (netherLevel != null) {
            evacuatedCount += evacuatePlayers(netherLevel, overworld)
        }

        // 엔드에 있는 플레이어 대피
        val endLevel = server.getLevel(Level.END)
        if (endLevel != null) {
            evacuatedCount += evacuatePlayers(endLevel, overworld)
        }

        // 고정 스폰 위치 초기화 (다음 진입 시 새 위치 생성)
        WildSpawnData.get(server).clearSpawn()

        // 야생 + 네더 + 엔드 차원 파일 삭제
        try {
            wildLevel.noSave = true
            val worldDir = server.getWorldPath(LevelResource.ROOT)

            // 야생 차원
            val wildDimDir = worldDir.resolve("dimensions").resolve("estherserver").resolve("wild").toFile()
            if (wildDimDir.exists()) {
                wildDimDir.deleteRecursively()
            }

            // 네더 (DIM-1)
            val netherDir = worldDir.resolve("DIM-1").toFile()
            if (netherDir.exists()) {
                netherDir.deleteRecursively()
            }

            // 엔드 (DIM1)
            val endDir = worldDir.resolve("DIM1").toFile()
            if (endDir.exists()) {
                endDir.deleteRecursively()
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

    private fun evacuatePlayers(fromLevel: ServerLevel, overworld: ServerLevel): Int {
        val players = fromLevel.players().toList()
        var count = 0
        for (player in players) {
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
                count++
            }
        }
        return count
    }
}
