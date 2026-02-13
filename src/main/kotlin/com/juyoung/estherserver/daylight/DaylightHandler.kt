package com.juyoung.estherserver.daylight

import com.juyoung.estherserver.Config
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent

object DaylightHandler {

    private const val TICKS_PER_DAY = 24000
    private const val DAY_END_TICK = 12000
    private var tickCounter = 0

    @SubscribeEvent
    fun onLevelTick(event: LevelTickEvent.Post) {
        val level = event.level
        if (level !is ServerLevel) return
        if (level.dimension() != Level.OVERWORLD) return
        if (!level.gameRules.getBoolean(GameRules.RULE_DAYLIGHT)) return

        val multiplier = Config.daytimeMultiplier
        if (multiplier <= 1) return

        val dayTime = level.dayTime % TICKS_PER_DAY

        if (dayTime in 0 until DAY_END_TICK) {
            tickCounter++
            if (tickCounter % multiplier != 0) {
                level.setDayTime(level.dayTime - 1)
            }
        } else {
            tickCounter = 0
        }
    }
}
