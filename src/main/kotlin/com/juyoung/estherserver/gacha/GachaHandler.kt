package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.economy.EconomyHandler
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

object GachaHandler {

    fun processGacha(player: ServerPlayer, pool: GachaRewardPool): Boolean {
        val entry = pool.roll() ?: return false

        when (entry.type) {
            RewardType.ITEM -> giveItemReward(player, entry)
            RewardType.CURRENCY -> giveCurrencyReward(player, entry)
        }

        playGachaEffect(player)
        return true
    }

    private fun giveItemReward(player: ServerPlayer, entry: GachaRewardEntry) {
        val stack = entry.itemSupplier?.get() ?: return
        val displayName = Component.translatable(entry.displayKey)

        if (!player.inventory.add(stack.copy())) {
            player.drop(stack.copy(), false)
        }

        player.sendSystemMessage(
            Component.translatable("message.estherserver.gacha_result_item", displayName)
        )
    }

    private fun giveCurrencyReward(player: ServerPlayer, entry: GachaRewardEntry) {
        EconomyHandler.addBalance(player, entry.currencyAmount)
        player.sendSystemMessage(
            Component.translatable(
                "message.estherserver.gacha_result_currency",
                entry.currencyAmount
            )
        )
    }

    private fun playGachaEffect(player: ServerPlayer) {
        player.level().playSound(
            null,
            player.blockPosition(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.8f,
            1.2f
        )
    }
}
