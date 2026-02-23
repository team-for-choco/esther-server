package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object OreVeinDetector {

    private const val SCAN_INTERVAL_TICKS = 40  // every 2 seconds

    /** 강화 레벨별 탐지 반경 (블록). 인덱스 = enhLevel - 5 */
    private val SCAN_RADIUS_BY_TIER = intArrayOf(
        4,  // Lv5: 반경 4블록
    )

    private fun getScanRadius(enhLevel: Int): Int {
        val tierIndex = (enhLevel - 5).coerceIn(0, SCAN_RADIUS_BY_TIER.lastIndex)
        return SCAN_RADIUS_BY_TIER[tierIndex]
    }

    // 광물별 파티클 색상 (0xRRGGBB)
    private fun getOreParticle(state: BlockState): DustParticleOptions? = when {
        state.`is`(BlockTags.COAL_ORES) -> dust(0x4D4D4D)
        state.`is`(BlockTags.IRON_ORES) -> dust(0xD9A088)
        state.`is`(BlockTags.COPPER_ORES) -> dust(0xE07850)
        state.`is`(BlockTags.GOLD_ORES) -> dust(0xFFD900)
        state.`is`(BlockTags.DIAMOND_ORES) -> dust(0x6BE6E6)
        state.`is`(BlockTags.EMERALD_ORES) -> dust(0x33E64D)
        state.`is`(BlockTags.LAPIS_ORES) -> dust(0x334DE6)
        state.`is`(BlockTags.REDSTONE_ORES) -> dust(0xE61A1A)
        else -> null
    }

    private fun dust(color: Int) = DustParticleOptions(color, 1.5f)

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        val server = event.server
        if (server.tickCount % SCAN_INTERVAL_TICKS != 0) return

        for (player in server.playerList.players) {
            scanForPlayer(player)
        }
    }

    private fun scanForPlayer(player: ServerPlayer) {
        val stack = player.mainHandItem
        if (stack.item !== EstherServerMod.SPECIAL_PICKAXE.get()) return

        val enhLevel = stack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
        if (enhLevel < 5) return

        val radius = getScanRadius(enhLevel)
        val level = player.serverLevel()
        val center = player.blockPosition()
        val playerEye = player.eyePosition

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val pos = center.offset(x, y, z)
                    val state = level.getBlockState(pos)
                    val particle = getOreParticle(state) ?: continue

                    val targetPos = findVisiblePosition(level, pos, playerEye) ?: continue
                    val packet = ClientboundLevelParticlesPacket(
                        particle,
                        false, // overrideLimiter
                        true,  // alwaysShow
                        targetPos.x + 0.5, targetPos.y + 0.5, targetPos.z + 0.5,
                        0.2f, 0.2f, 0.2f,
                        0.01f,
                        3
                    )
                    player.connection.send(packet)
                }
            }
        }
    }

    /** 광석 위치에서 플레이어 눈 방향으로 추적하여 첫 번째 비고체 블록을 찾는다 */
    private fun findVisiblePosition(level: ServerLevel, orePos: BlockPos, playerEye: Vec3): BlockPos? {
        val start = Vec3(orePos.x + 0.5, orePos.y + 0.5, orePos.z + 0.5)
        val dir = playerEye.subtract(start)
        val maxDist = dir.length()
        if (maxDist < 0.1) return orePos

        val normalized = dir.normalize()
        var step = 0.5
        while (step <= maxDist) {
            val point = start.add(normalized.scale(step))
            val checkPos = BlockPos.containing(point.x, point.y, point.z)
            if (checkPos != orePos && !level.getBlockState(checkPos).isSolidRender()) {
                return checkPos
            }
            step += 0.5
        }
        return null
    }
}
