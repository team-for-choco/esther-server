package com.juyoung.estherserver.quest

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object QuestCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("quest")
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("reset")
                                .then(
                                    Commands.literal("daily")
                                        .executes { context ->
                                            resetQuests(context.source, context.source.playerOrException, daily = true, weekly = false)
                                        }
                                        .then(
                                            Commands.argument("player", EntityArgument.player())
                                                .executes { context ->
                                                    resetQuests(context.source, EntityArgument.getPlayer(context, "player"), daily = true, weekly = false)
                                                }
                                        )
                                )
                                .then(
                                    Commands.literal("weekly")
                                        .executes { context ->
                                            resetQuests(context.source, context.source.playerOrException, daily = false, weekly = true)
                                        }
                                        .then(
                                            Commands.argument("player", EntityArgument.player())
                                                .executes { context ->
                                                    resetQuests(context.source, EntityArgument.getPlayer(context, "player"), daily = false, weekly = true)
                                                }
                                        )
                                )
                                .then(
                                    Commands.literal("all")
                                        .executes { context ->
                                            resetQuests(context.source, context.source.playerOrException, daily = true, weekly = true)
                                        }
                                        .then(
                                            Commands.argument("player", EntityArgument.player())
                                                .executes { context ->
                                                    resetQuests(context.source, EntityArgument.getPlayer(context, "player"), daily = true, weekly = true)
                                                }
                                        )
                                )
                        )
                        .then(
                            Commands.literal("progress")
                                .then(
                                    Commands.argument("tracking_type", StringArgumentType.word())
                                        .then(
                                            Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                                .executes { context ->
                                                    val player = context.source.playerOrException
                                                    val typeStr = StringArgumentType.getString(context, "tracking_type")
                                                    val amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount")
                                                    val trackingType = try {
                                                        QuestTrackingType.valueOf(typeStr.uppercase())
                                                    } catch (_: Exception) {
                                                        context.source.sendFailure(Component.literal("Invalid tracking type: $typeStr"))
                                                        return@executes 0
                                                    }
                                                    QuestHandler.trackProgress(player, trackingType, amount, null)
                                                    context.source.sendSuccess({ Component.literal("Added $amount progress to $typeStr") }, true)
                                                    1
                                                }
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("info")
                        .executes { context ->
                            showQuestInfo(context.source, context.source.playerOrException)
                        }
                )
        )
    }

    private fun resetQuests(source: CommandSourceStack, player: ServerPlayer, daily: Boolean, weekly: Boolean): Int {
        QuestHandler.resetQuests(player, daily, weekly)
        val type = when {
            daily && weekly -> "일일+주간"
            daily -> "일일"
            else -> "주간"
        }
        source.sendSuccess({ Component.literal("${player.name.string}의 ${type} 퀘스트를 리셋했습니다.") }, true)
        return 1
    }

    private fun showQuestInfo(source: CommandSourceStack, player: ServerPlayer): Int {
        val data = player.getData(ModQuest.QUEST_DATA.get())

        source.sendSuccess({ Component.literal("=== 일일 퀘스트 (${data.getDailyClaimedCount()}/3 수령) ===") }, false)
        for ((i, quest) in data.dailyQuests.withIndex()) {
            val template = QuestPool.getTemplate(quest.templateId)
            val status = when {
                quest.claimed -> "[수령완료]"
                template != null && quest.isComplete(template) -> "[완료]"
                else -> "[진행중]"
            }
            val target = template?.targetCount ?: 0
            source.sendSuccess({ Component.literal("  ${i + 1}. ${quest.templateId} ${quest.progress}/$target $status") }, false)
        }

        source.sendSuccess({ Component.literal("=== 주간 퀘스트 (${data.getWeeklyClaimedCount()}/3 수령) ===") }, false)
        for ((i, quest) in data.weeklyQuests.withIndex()) {
            val template = QuestPool.getTemplate(quest.templateId)
            val status = when {
                quest.claimed -> "[수령완료]"
                template != null && quest.isComplete(template) -> "[완료]"
                else -> "[진행중]"
            }
            val target = template?.targetCount ?: 0
            source.sendSuccess({ Component.literal("  ${i + 1}. ${quest.templateId} ${quest.progress}/$target $status") }, false)
        }

        return 1
    }
}
