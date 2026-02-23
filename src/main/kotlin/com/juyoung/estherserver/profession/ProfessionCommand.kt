package com.juyoung.estherserver.profession

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object ProfessionCommand {

    private val PROFESSION_NAMES = Profession.entries.map { it.name.lowercase() }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("profession")
                .executes { context -> showStatus(context.source) }
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("setlevel")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players.map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .then(
                                            Commands.argument("profession", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    SharedSuggestionProvider.suggest(PROFESSION_NAMES, builder)
                                                }
                                                .then(
                                                    Commands.argument("level", IntegerArgumentType.integer(0, Profession.MAX_LEVEL))
                                                        .executes { context ->
                                                            adminSetLevel(
                                                                context.source,
                                                                StringArgumentType.getString(context, "player"),
                                                                StringArgumentType.getString(context, "profession"),
                                                                IntegerArgumentType.getInteger(context, "level")
                                                            )
                                                        }
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("addxp")
                                .then(
                                    Commands.argument("player", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players.map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .then(
                                            Commands.argument("profession", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    SharedSuggestionProvider.suggest(PROFESSION_NAMES, builder)
                                                }
                                                .then(
                                                    Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes { context ->
                                                            adminAddXp(
                                                                context.source,
                                                                StringArgumentType.getString(context, "player"),
                                                                StringArgumentType.getString(context, "profession"),
                                                                IntegerArgumentType.getInteger(context, "amount")
                                                            )
                                                        }
                                                )
                                        )
                                )
                        )
                )
        )
    }

    private fun showStatus(source: CommandSourceStack): Int {
        val player = source.playerOrException
        val serverPlayer = player as ServerPlayer
        source.sendSuccess({
            Component.translatable("message.estherserver.profession_status_header")
        }, false)
        for (profession in Profession.entries) {
            val level = ProfessionHandler.getLevel(serverPlayer, profession)
            val xp = ProfessionHandler.getXp(serverPlayer, profession)
            val requiredXp = if (level < Profession.MAX_LEVEL) Profession.getRequiredXp(level + 1) else 0
            source.sendSuccess({
                Component.translatable(
                    "message.estherserver.profession_status_entry",
                    Component.translatable(profession.translationKey),
                    level,
                    xp,
                    requiredXp
                )
            }, false)
        }
        return 1
    }

    private fun parseProfession(name: String): Profession? {
        return Profession.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
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

    private fun adminSetLevel(source: CommandSourceStack, targetName: String, professionName: String, level: Int): Int {
        val profession = parseProfession(professionName)
        if (profession == null) {
            source.sendFailure(Component.translatable("message.estherserver.profession_invalid", professionName))
            return 0
        }
        return withOnlinePlayer(source, targetName) { target ->
            val data = target.getData(ModProfession.PROFESSION_DATA.get())
            data.setLevel(profession, level)
            data.setXp(profession, 0)
            target.setData(ModProfession.PROFESSION_DATA.get(), data)
            ProfessionHandler.syncToClient(target)
            source.sendSuccess({
                Component.translatable(
                    "message.estherserver.profession_admin_setlevel",
                    target.displayName,
                    Component.translatable(profession.translationKey),
                    level
                )
            }, false)
            1
        }
    }

    private fun adminAddXp(source: CommandSourceStack, targetName: String, professionName: String, amount: Int): Int {
        val profession = parseProfession(professionName)
        if (profession == null) {
            source.sendFailure(Component.translatable("message.estherserver.profession_invalid", professionName))
            return 0
        }
        return withOnlinePlayer(source, targetName) { target ->
            ProfessionHandler.addExperience(target, profession, amount)
            source.sendSuccess({
                Component.translatable(
                    "message.estherserver.profession_admin_addxp",
                    amount,
                    Component.translatable(profession.translationKey),
                    target.displayName
                )
            }, false)
            1
        }
    }
}
