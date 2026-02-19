package com.juyoung.estherserver.economy

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object MoneyCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("money")
                .executes { context -> showBalance(context.source) }
                .then(
                    Commands.literal("pay")
                        .then(
                            Commands.argument("player", StringArgumentType.word())
                                .suggests { context, builder ->
                                    SharedSuggestionProvider.suggest(
                                        context.source.server.playerList.players
                                            .filter { it != context.source.player }
                                            .map { it.gameProfile.name },
                                        builder
                                    )
                                }
                                .then(
                                    Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes { context ->
                                            pay(
                                                context.source,
                                                StringArgumentType.getString(context, "player"),
                                                LongArgumentType.getLong(context, "amount")
                                            )
                                        }
                                )
                        )
                )
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players.map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .then(
                                            Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes { context ->
                                                    adminAdd(
                                                        context.source,
                                                        StringArgumentType.getString(context, "player"),
                                                        LongArgumentType.getLong(context, "amount")
                                                    )
                                                }
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players.map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .then(
                                            Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes { context ->
                                                    adminRemove(
                                                        context.source,
                                                        StringArgumentType.getString(context, "player"),
                                                        LongArgumentType.getLong(context, "amount")
                                                    )
                                                }
                                        )
                                )
                        )
                        .then(
                            Commands.literal("set")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players.map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .then(
                                            Commands.argument("amount", LongArgumentType.longArg(0))
                                                .executes { context ->
                                                    adminSet(
                                                        context.source,
                                                        StringArgumentType.getString(context, "player"),
                                                        LongArgumentType.getLong(context, "amount")
                                                    )
                                                }
                                        )
                                )
                        )
                )
        )
    }

    private fun showBalance(source: CommandSourceStack): Int {
        val player = source.playerOrException
        val balance = EconomyHandler.getBalance(player)
        source.sendSuccess({
            Component.translatable("message.estherserver.money_balance", balance)
        }, false)
        return 1
    }

    private fun pay(source: CommandSourceStack, targetName: String, amount: Long): Int {
        val sender = source.playerOrException
        val target = source.server.playerList.getPlayerByName(targetName)
        if (target == null) {
            source.sendFailure(Component.translatable("message.estherserver.money_player_not_found", targetName))
            return 0
        }
        if (target == sender) {
            source.sendFailure(Component.translatable("message.estherserver.money_pay_self"))
            return 0
        }
        if (!EconomyHandler.removeBalance(sender, amount)) {
            source.sendFailure(Component.translatable("message.estherserver.money_insufficient"))
            return 0
        }
        EconomyHandler.addBalance(target, amount)
        source.sendSuccess({
            Component.translatable("message.estherserver.money_pay_sent", amount, target.displayName)
        }, false)
        target.sendSystemMessage(
            Component.translatable("message.estherserver.money_pay_received", amount, sender.displayName)
        )
        return 1
    }

    private fun withOnlinePlayer(
        source: CommandSourceStack,
        targetName: String,
        action: (ServerPlayer) -> Int
    ): Int {
        val target = source.server.playerList.getPlayerByName(targetName)
        if (target == null) {
            source.sendFailure(Component.translatable("message.estherserver.money_player_not_found", targetName))
            return 0
        }
        return action(target)
    }

    private fun adminAdd(source: CommandSourceStack, targetName: String, amount: Long): Int =
        withOnlinePlayer(source, targetName) { target ->
            EconomyHandler.addBalance(target, amount)
            source.sendSuccess({
                Component.translatable("message.estherserver.money_admin_add", amount, target.displayName)
            }, false)
            1
        }

    private fun adminRemove(source: CommandSourceStack, targetName: String, amount: Long): Int =
        withOnlinePlayer(source, targetName) { target ->
            if (!EconomyHandler.removeBalance(target, amount)) {
                source.sendFailure(Component.translatable("message.estherserver.money_admin_insufficient", target.displayName))
                return@withOnlinePlayer 0
            }
            source.sendSuccess({
                Component.translatable("message.estherserver.money_admin_remove", amount, target.displayName)
            }, false)
            1
        }

    private fun adminSet(source: CommandSourceStack, targetName: String, amount: Long): Int =
        withOnlinePlayer(source, targetName) { target ->
            EconomyHandler.setBalance(target, amount)
            source.sendSuccess({
                Component.translatable("message.estherserver.money_admin_set", target.displayName, amount)
            }, false)
            1
        }
}
