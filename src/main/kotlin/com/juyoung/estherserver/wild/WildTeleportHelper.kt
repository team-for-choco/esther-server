package com.juyoung.estherserver.wild

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap
import java.util.EnumSet
import net.minecraft.world.entity.RelativeMovement

object WildTeleportHelper {

    private const val SPAWN_RANGE = 5000
    private const val MAX_ATTEMPTS = 10
    private const val MIN_Y = 63

    fun teleportToWild(player: ServerPlayer): Boolean {
        val server = player.server
        val wildLevel = server.getLevel(WildDimensionKeys.WILD_LEVEL) ?: return false

        // 현재 오버월드 좌표 저장
        val returnData = WildReturnData(
            x = player.x,
            y = player.y,
            z = player.z,
            yaw = player.yRot,
            pitch = player.xRot,
            hasData = true
        )
        player.setData(ModWild.RETURN_DATA.get(), returnData)

        // 고정 스폰 위치 조회 (없으면 생성 후 저장)
        val spawnData = WildSpawnData.get(server)
        val safePos = spawnData.getSpawnPos() ?: run {
            val newPos = findSafeLocation(wildLevel) ?: return false
            spawnData.setSpawnPos(newPos)
            newPos
        }

        // 텔레포트
        player.teleportTo(
            wildLevel,
            safePos.x.toDouble() + 0.5,
            safePos.y.toDouble(),
            safePos.z.toDouble() + 0.5,
            EnumSet.noneOf(RelativeMovement::class.java),
            player.yRot,
            player.xRot,
            false
        )

        // 착지 위치 아래에 귀환 포탈 배치 (발밑 블록 교체)
        val portalPos = BlockPos(safePos.x, safePos.y - 1, safePos.z)
        val returnPortalBlock = com.juyoung.estherserver.EstherServerMod.RETURN_PORTAL.get()
        wildLevel.setBlock(portalPos, returnPortalBlock.defaultBlockState(), 3)

        player.displayClientMessage(
            Component.translatable("message.estherserver.wild_teleported"), false
        )

        return true
    }

    fun teleportToOverworld(player: ServerPlayer): Boolean {
        val server = player.server
        val overworld = server.getLevel(Level.OVERWORLD) ?: return false

        val returnData = player.getData(ModWild.RETURN_DATA.get())

        val targetX: Double
        val targetY: Double
        val targetZ: Double
        val targetYaw: Float
        val targetPitch: Float

        if (returnData.hasData) {
            targetX = returnData.x
            targetY = returnData.y
            targetZ = returnData.z
            targetYaw = returnData.yaw
            targetPitch = returnData.pitch
        } else {
            val spawn = overworld.sharedSpawnPos
            targetX = spawn.x.toDouble() + 0.5
            targetY = spawn.y.toDouble()
            targetZ = spawn.z.toDouble() + 0.5
            targetYaw = player.yRot
            targetPitch = player.xRot
        }

        player.teleportTo(
            overworld,
            targetX,
            targetY,
            targetZ,
            EnumSet.noneOf(RelativeMovement::class.java),
            targetYaw,
            targetPitch,
            false
        )

        player.displayClientMessage(
            Component.translatable("message.estherserver.wild_returned"), false
        )

        return true
    }

    fun findSafeLocation(level: ServerLevel): BlockPos? {
        val random = level.random
        for (i in 0 until MAX_ATTEMPTS) {
            val x = random.nextInt(SPAWN_RANGE * 2) - SPAWN_RANGE
            val z = random.nextInt(SPAWN_RANGE * 2) - SPAWN_RANGE

            // 청크 로드 확인
            val chunk = level.getChunk(x shr 4, z shr 4)

            val surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x and 15, z and 15) + 1

            if (surfaceY < MIN_Y) continue

            val pos = BlockPos(x, surfaceY, z)
            val belowPos = pos.below()

            // 발밑이 고체 블록이고, 발+머리가 공기인지 확인
            val belowState = level.getBlockState(belowPos)
            val feetState = level.getBlockState(pos)
            val headState = level.getBlockState(pos.above())

            if (belowState.isFaceSturdy(level, belowPos, Direction.UP) && feetState.isAir && headState.isAir) {
                return pos
            }
        }
        return null
    }
}
