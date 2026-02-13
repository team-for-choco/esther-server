package com.juyoung.estherserver.collection

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TitleCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("title")
                .then(
                    Commands.literal("select")
                        .then(
                            Commands.argument("id", StringArgumentType.word())
                                .suggests { context, builder -> suggestUnlockedMilestones(context, builder) }
                                .executes { context -> selectTitle(context) }
                        )
                )
                .then(
                    Commands.literal("clear")
                        .executes { context -> clearTitle(context) }
                )
                .then(
                    Commands.literal("list")
                        .executes { context -> listTitles(context) }
                )
        )
    }

    private fun suggestUnlockedMilestones(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ) = SharedSuggestionProvider.suggest(
        run {
            val player = context.source.playerOrException
            val data = player.getData(ModCollection.COLLECTION_DATA.get())
            data.unlockedMilestones.toList()
        },
        builder
    )

    private fun selectTitle(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val milestoneId = StringArgumentType.getString(context, "id")
        CollectionHandler.handleTitleSelect(player, milestoneId)
        return 1
    }

    private fun clearTitle(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        CollectionHandler.handleTitleSelect(player, "")
        return 1
    }

    private fun listTitles(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val data = player.getData(ModCollection.COLLECTION_DATA.get())

        player.displayClientMessage(
            Component.translatable("message.estherserver.title_list_header"), false
        )

        for (milestone in Milestone.entries) {
            val unlocked = milestone.id in data.unlockedMilestones
            val isActive = data.activeTitle == milestone.id
            val titleName = Component.translatable(milestone.titleKey)
            val desc = Component.translatable(milestone.descriptionKey)

            if (unlocked) {
                val activeMarker = if (isActive) " [*]" else ""
                player.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.title_list_unlocked",
                        titleName, desc
                    ).append(Component.literal(activeMarker)),
                    false
                )
            } else {
                val progress = milestone.progressProvider?.invoke(data)
                if (progress != null) {
                    player.displayClientMessage(
                        Component.translatable(
                            "message.estherserver.title_list_locked_progress",
                            titleName, desc, progress.first, progress.second
                        ),
                        false
                    )
                } else {
                    player.displayClientMessage(
                        Component.translatable(
                            "message.estherserver.title_list_locked",
                            titleName, desc
                        ),
                        false
                    )
                }
            }
        }
        return 1
    }
}
