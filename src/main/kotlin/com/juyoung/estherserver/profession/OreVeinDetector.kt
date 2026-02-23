package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
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

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val pos = center.offset(x, y, z)
                    val state = level.getBlockState(pos)
                    if (state.`is`(BlockTags.COAL_ORES) ||
                        state.`is`(BlockTags.IRON_ORES) ||
                        state.`is`(BlockTags.COPPER_ORES) ||
                        state.`is`(BlockTags.GOLD_ORES) ||
                        state.`is`(BlockTags.DIAMOND_ORES) ||
                        state.`is`(BlockTags.EMERALD_ORES) ||
                        state.`is`(BlockTags.LAPIS_ORES) ||
                        state.`is`(BlockTags.REDSTONE_ORES)
                    ) {
                        val packet = ClientboundLevelParticlesPacket(
                            ParticleTypes.ELECTRIC_SPARK,
                            false, // overrideLimiter
                            true,  // alwaysShow
                            pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5,
                            0.3f, 0.3f, 0.3f,
                            0.02f,
                            5
                        )
                        player.connection.send(packet)
                    }
                }
            }
        }
    }
}
