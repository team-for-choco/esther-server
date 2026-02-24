package com.juyoung.estherserver.item

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.mixin.FishingHookAccessor
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

object AutoFishHandler {

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        for (player in event.server.playerList.players) {
            tryAutoRetrieve(player)
        }
    }

    private fun tryAutoRetrieve(player: ServerPlayer) {
        val hook = player.fishing ?: return

        if (!(hook as FishingHookAccessor).`estherserver$isBiting`()) return

        val rod = player.mainHandItem
        if (rod.item !== EstherServerMod.SPECIAL_FISHING_ROD.get()) return

        val enhLevel = rod.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
        if (enhLevel < 5) return

        hook.retrieve(rod)

        player.level().playSound(
            null, player.x, player.y, player.z,
            SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
            1.0f, 0.4f / (player.random.nextFloat() * 0.4f + 0.8f)
        )
    }
}
