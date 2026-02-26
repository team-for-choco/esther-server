package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.economy.EconomyHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.neoforged.neoforge.network.PacketDistributor

object GachaHandler {

    fun processGacha(player: ServerPlayer, pool: GachaRewardPool): Boolean {
        val entry = pool.roll() ?: return false

        when (entry.type) {
            RewardType.ITEM -> giveItemReward(player, entry)
            RewardType.CURRENCY -> giveCurrencyReward(player, entry)
        }

        playGachaEffect(player)
        sendRoulettePayload(player, pool, entry)
        return true
    }

    private fun giveItemReward(player: ServerPlayer, entry: GachaRewardEntry) {
        val stack = entry.itemSupplier?.get() ?: return

        if (!player.inventory.add(stack.copy())) {
            player.drop(stack.copy(), false)
        }
    }

    private fun giveCurrencyReward(player: ServerPlayer, entry: GachaRewardEntry) {
        EconomyHandler.addBalance(player, entry.currencyAmount)
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

    private fun sendRoulettePayload(player: ServerPlayer, pool: GachaRewardPool, winnerEntry: GachaRewardEntry) {
        val poolEntries = pool.getEntries()
        val displayEntries = poolEntries.map { toDisplayEntry(it) }

        val winnerIndex = poolEntries.indexOf(winnerEntry).coerceAtLeast(0)

        val payload = GachaRoulettePayload(
            entries = displayEntries,
            winnerIndex = winnerIndex,
            poolId = pool.id
        )

        PacketDistributor.sendToPlayer(player, payload)
    }

    private fun toDisplayEntry(entry: GachaRewardEntry): RouletteDisplayEntry {
        val itemId = entry.displayItemId.ifEmpty {
            when (entry.type) {
                RewardType.CURRENCY -> "minecraft:gold_ingot"
                RewardType.ITEM -> "minecraft:barrier"
            }
        }
        val count = when (entry.type) {
            RewardType.CURRENCY -> 1
            RewardType.ITEM -> entry.itemSupplier?.get()?.count ?: 1
        }
        return RouletteDisplayEntry(
            itemId = itemId,
            count = count,
            displayKey = entry.displayKey,
            isCurrency = entry.type == RewardType.CURRENCY,
            currencyAmount = entry.currencyAmount
        )
    }
}
