package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object OreVeinDetector {

    private const val SCAN_INTERVAL_TICKS = 40  // every 2 seconds
    private const val SCAN_RADIUS = 8

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

        val level = player.serverLevel()
        val center = player.blockPosition()

        for (x in -SCAN_RADIUS..SCAN_RADIUS) {
            for (y in -SCAN_RADIUS..SCAN_RADIUS) {
                for (z in -SCAN_RADIUS..SCAN_RADIUS) {
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
                        level.sendParticles(
                            ParticleTypes.ENCHANT,
                            pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                            2, 0.2, 0.2, 0.2, 0.01
                        )
                    }
                }
            }
        }
    }
}
