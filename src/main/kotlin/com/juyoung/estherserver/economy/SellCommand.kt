package com.juyoung.estherserver.economy

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object SellCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("sell")
                .executes { context -> sellHeldItem(context.source) }
        )
    }

    private fun sellHeldItem(source: CommandSourceStack): Int {
        val player = source.playerOrException
        val stack = player.mainHandItem

        if (stack.isEmpty) {
            source.sendFailure(Component.translatable("message.estherserver.sell_empty_hand"))
            return 0
        }

        val pricePerItem = ItemPriceRegistry.getPrice(stack)
        if (pricePerItem == null) {
            source.sendFailure(Component.translatable("message.estherserver.sell_no_price"))
            return 0
        }

        val count = stack.count
        val totalPrice = pricePerItem * count
        val itemName = stack.hoverName

        stack.shrink(count)
        EconomyHandler.addBalance(player, totalPrice)

        source.sendSuccess({
            Component.translatable(
                "message.estherserver.sell_success",
                count,
                itemName,
                totalPrice
            )
        }, false)

        return 1
    }
}
