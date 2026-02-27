package com.juyoung.estherserver.cosmetic

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level

class CosmeticTokenItem(
    private val cosmeticId: String,
    properties: Properties
) : Item(properties) {

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        val stack = player.getItemInHand(usedHand)
        val data = serverPlayer.getData(ModCosmetics.COSMETIC_DATA.get())

        val def = CosmeticRegistry.get(cosmeticId)
        if (def == null) {
            serverPlayer.sendSystemMessage(Component.literal("알 수 없는 치장입니다."))
            return InteractionResult.FAIL
        }

        if (cosmeticId in data.unlockedCosmetics) {
            // 이미 보유 → 보상 (화폐 100)
            val balanceData = serverPlayer.getData(com.juyoung.estherserver.economy.ModEconomy.BALANCE_DATA.get())
            balanceData.balance += 100
            serverPlayer.setData(com.juyoung.estherserver.economy.ModEconomy.BALANCE_DATA.get(), balanceData)
            com.juyoung.estherserver.economy.EconomyHandler.syncToClient(serverPlayer)
            serverPlayer.sendSystemMessage(
                Component.translatable("message.estherserver.cosmetic_duplicate", Component.translatable(def.displayKey))
            )
        } else {
            data.unlockedCosmetics.add(cosmeticId)
            serverPlayer.setData(ModCosmetics.COSMETIC_DATA.get(), data)
            serverPlayer.sendSystemMessage(
                Component.translatable("message.estherserver.cosmetic_unlocked", Component.translatable(def.displayKey))
            )
        }

        stack.shrink(1)
        return InteractionResult.SUCCESS
    }
}
