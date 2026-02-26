package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.pet.ModPets
import com.juyoung.estherserver.pet.PetType
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack

object GachaHandler {

    private val PET_DUPLICATE_CURRENCY = mapOf(
        com.juyoung.estherserver.pet.PetGrade.COMMON to 500L,
        com.juyoung.estherserver.pet.PetGrade.FINE to 1500L,
        com.juyoung.estherserver.pet.PetGrade.RARE to 5000L
    )

    fun processGacha(player: ServerPlayer, pool: GachaRewardPool): Boolean {
        val entry = pool.roll() ?: return false

        when (entry.type) {
            RewardType.ITEM -> giveItemReward(player, entry)
            RewardType.PET -> givePetReward(player, entry)
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

    private fun givePetReward(player: ServerPlayer, entry: GachaRewardEntry) {
        val petType = entry.petType ?: return
        val petData = player.getData(ModPets.PET_DATA.get())
        val displayName = Component.translatable(entry.displayKey)

        if (petType in petData.ownedPets) {
            val compensation = PET_DUPLICATE_CURRENCY[petType.grade] ?: 500L
            EconomyHandler.addBalance(player, compensation)
            player.sendSystemMessage(
                Component.translatable(
                    "message.estherserver.gacha_pet_duplicate",
                    displayName,
                    compensation
                )
            )
        } else {
            petData.ownedPets.add(petType)
            player.setData(ModPets.PET_DATA.get(), petData)
            player.sendSystemMessage(
                Component.translatable("message.estherserver.gacha_result_pet", displayName)
            )
        }
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
