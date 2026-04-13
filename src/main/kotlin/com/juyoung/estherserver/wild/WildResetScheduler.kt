package com.juyoung.estherserver.wild

import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.EnumSet
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import net.minecraft.world.entity.Relative

object WildResetScheduler {

    private val LOGGER = LoggerFactory.getLogger("WildResetScheduler")
    private val KST = ZoneId.of("Asia/Seoul")
    private var scheduler: ScheduledExecutorService? = null

    fun start(server: MinecraftServer) {
        shutdown()
        scheduler = Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "WildResetScheduler").apply { isDaemon = true }
        }

        scheduleNextReset(server)
    }

    fun shutdown() {
        scheduler?.shutdownNow()
        scheduler = null
    }

    private fun scheduleNextReset(server: MinecraftServer) {
        val now = ZonedDateTime.now(KST)
        val nextMonday = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0).withNano(0)
        val delayMillis = java.time.Duration.between(now, nextMonday).toMillis()

        LOGGER.info(
            "야생 자동 리셋 예약: {}ms 후 ({})",
            delayMillis, nextMonday
        )

        scheduler?.schedule({
            server.execute {
                executeReset(server)
                scheduleNextReset(server)
            }
        }, delayMillis, TimeUnit.MILLISECONDS)
    }

    fun executeReset(server: MinecraftServer) {
        val wildLevel = server.getLevel(WildDimensionKeys.WILD_LEVEL)
        if (wildLevel == null) {
            LOGGER.warn("야생 자동 리셋 실패: 야생 차원을 찾을 수 없음")
            return
        }

        val overworld = server.getLevel(Level.OVERWORLD) ?: return

        // 야생 차원 플레이어 대피
        val playersInWild = wildLevel.players().toList()
        for (player in playersInWild) {
            if (player is ServerPlayer) {
                evacuatePlayer(player, overworld)
            }
        }

        // 스폰 초기화
        WildSpawnData.get(server).clearSpawn()

        // 야생 차원 파일 삭제
        try {
            wildLevel.noSave = true
            val worldDir = server.getWorldPath(LevelResource.ROOT)
            val wildDimDir = worldDir.resolve("dimensions").resolve("estherserver").resolve("wild").toFile()
            if (wildDimDir.exists()) {
                wildDimDir.deleteRecursively()
            }
        } catch (e: Exception) {
            LOGGER.error("야생 자동 리셋 파일 삭제 실패", e)
            return
        }

        server.playerList.broadcastSystemMessage(
            Component.translatable("message.estherserver.wild_reset_broadcast"), false
        )
        LOGGER.info("야생 자동 리셋 완료")
    }

    private fun evacuatePlayer(player: ServerPlayer, overworld: net.minecraft.server.level.ServerLevel) {
        val returnData = player.getData(ModWild.RETURN_DATA.get())
        if (returnData.hasData) {
            player.teleportTo(
                overworld,
                returnData.x, returnData.y, returnData.z,
                EnumSet.noneOf(Relative::class.java),
                returnData.yaw, returnData.pitch, false
            )
        } else {
            val spawn = overworld.sharedSpawnPos
            player.teleportTo(
                overworld,
                spawn.x.toDouble() + 0.5, spawn.y.toDouble(), spawn.z.toDouble() + 0.5,
                EnumSet.noneOf(Relative::class.java),
                player.yRot, player.xRot, false
            )
        }
    }
}
