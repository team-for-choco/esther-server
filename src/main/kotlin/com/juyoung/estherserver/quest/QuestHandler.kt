package com.juyoung.estherserver.quest

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.profession.ProfessionBonusHelper.ContentGrade
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.network.PacketDistributor
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

object QuestHandler {

    private val KST = ZoneId.of("Asia/Seoul")
    private const val MAX_CLAIMS = 3
    private const val DAILY_BONUS_CURRENCY = 50
    private const val WEEKLY_BONUS_CURRENCY = 200
    private const val DAILY_BONUS_SOUP_COUNT = 1
    private const val WEEKLY_BONUS_SOUP_COUNT = 3

    private var lastCheckTick = 0L

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        checkAndResetQuests(player)
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        val server = event.server
        lastCheckTick++
        // Check every 60 seconds (1200 ticks)
        if (lastCheckTick % 1200 != 0L) return

        for (player in server.playerList.players) {
            checkAndResetQuests(player)
        }
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        PacketDistributor.sendToPlayer(player, QuestSyncPayload(data))
    }

    /**
     * Calculate the "day number" for a given instant in KST timezone.
     * This gives a unique number for each day, used as seed for quest selection.
     */
    private fun getDayNumber(instant: Instant): Long {
        val kstTime = ZonedDateTime.ofInstant(instant, KST)
        return kstTime.toLocalDate().toEpochDay()
    }

    /**
     * Calculate the "week number" — epoch day of the Monday of that week.
     */
    private fun getWeekStartDay(instant: Instant): Long {
        val kstTime = ZonedDateTime.ofInstant(instant, KST)
        val dayOfWeek = kstTime.get(ChronoField.DAY_OF_WEEK) // 1=Monday
        return kstTime.toLocalDate().toEpochDay() - (dayOfWeek - 1)
    }

    fun checkAndResetQuests(player: ServerPlayer) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        val now = Instant.now()
        val currentDay = getDayNumber(now)
        val currentWeekStart = getWeekStartDay(now)
        var changed = false

        // Daily reset
        if (data.dailyResetDay != currentDay) {
            data.dailyQuests.clear()
            data.dailyBonusClaimed = false
            val seed = currentDay * 31 + player.server.overworld().seed
            val templates = QuestPool.selectQuests(seed, weekly = false)
            for (template in templates) {
                data.dailyQuests.add(ActiveQuest(template.id))
            }
            data.dailyResetDay = currentDay
            changed = true
        }

        // Weekly reset
        if (data.weeklyResetDay != currentWeekStart) {
            data.weeklyQuests.clear()
            data.weeklyBonusClaimed = false
            val seed = currentWeekStart * 53 + player.server.overworld().seed
            val templates = QuestPool.selectQuests(seed, weekly = true)
            for (template in templates) {
                data.weeklyQuests.add(ActiveQuest(template.id))
            }
            data.weeklyResetDay = currentWeekStart
            changed = true
        }

        if (changed) {
            player.setData(ModQuest.QUEST_DATA.get(), data)
            syncToClient(player)
        }
    }

    /**
     * Track progress from various game systems.
     * @param gradeStr grade string for grade-filtered quests: "COMMON", "ADVANCED", "RARE"
     */
    fun trackProgress(player: ServerPlayer, trackingType: QuestTrackingType, amount: Int, gradeStr: String?) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        var changed = false

        val grade = gradeStr?.let {
            try { ContentGrade.valueOf(it) } catch (_: Exception) { null }
        }

        // Track daily quests
        for (quest in data.dailyQuests) {
            if (quest.claimed) continue
            val template = QuestPool.getTemplate(quest.templateId) ?: continue
            if (template.trackingType != trackingType) continue
            if (template.gradeFilter != null && (grade == null || grade < template.gradeFilter)) continue
            quest.progress = (quest.progress + amount).coerceAtMost(template.targetCount)
            changed = true
        }

        // Track weekly quests
        for (quest in data.weeklyQuests) {
            if (quest.claimed) continue
            val template = QuestPool.getTemplate(quest.templateId) ?: continue
            if (template.trackingType != trackingType) continue
            if (template.gradeFilter != null && (grade == null || grade < template.gradeFilter)) continue
            quest.progress = (quest.progress + amount).coerceAtMost(template.targetCount)
            changed = true
        }

        if (changed) {
            player.setData(ModQuest.QUEST_DATA.get(), data)
            syncToClient(player)
        }
    }

    fun handleClaimQuest(player: ServerPlayer, questIndex: Int, isWeekly: Boolean) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        val questList = if (isWeekly) data.weeklyQuests else data.dailyQuests
        val claimedCount = if (isWeekly) data.getWeeklyClaimedCount() else data.getDailyClaimedCount()

        if (questIndex < 0 || questIndex >= questList.size) return
        val quest = questList[questIndex]
        if (quest.claimed) return
        if (claimedCount >= MAX_CLAIMS) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_max_claimed"), true
            )
            return
        }

        val template = QuestPool.getTemplate(quest.templateId) ?: return
        if (!quest.isComplete(template)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_not_complete"), true
            )
            return
        }

        quest.claimed = true

        // Give rewards
        EconomyHandler.addBalance(player, template.baseCurrencyReward.toLong(), skipQuestTracking = true)
        giveHuntersPot(player, 1)

        player.displayClientMessage(
            Component.translatable("message.estherserver.quest_claimed",
                template.baseCurrencyReward,
                1
            ), false
        )

        player.level().playSound(
            null, player.blockPosition(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
            0.7f, 1.5f
        )

        player.setData(ModQuest.QUEST_DATA.get(), data)
        syncToClient(player)
    }

    fun handleBonusClaim(player: ServerPlayer, isWeekly: Boolean) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        val claimedCount = if (isWeekly) data.getWeeklyClaimedCount() else data.getDailyClaimedCount()
        val bonusClaimed = if (isWeekly) data.weeklyBonusClaimed else data.dailyBonusClaimed

        if (claimedCount < MAX_CLAIMS) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_bonus_not_ready"), true
            )
            return
        }

        if (bonusClaimed) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_bonus_already_claimed"), true
            )
            return
        }

        val currency = if (isWeekly) WEEKLY_BONUS_CURRENCY else DAILY_BONUS_CURRENCY
        val soupCount = if (isWeekly) WEEKLY_BONUS_SOUP_COUNT else DAILY_BONUS_SOUP_COUNT

        if (isWeekly) data.weeklyBonusClaimed = true else data.dailyBonusClaimed = true

        EconomyHandler.addBalance(player, currency.toLong(), skipQuestTracking = true)
        giveHuntersPot(player, soupCount)

        player.displayClientMessage(
            Component.translatable("message.estherserver.quest_bonus_claimed", currency, soupCount), false
        )

        player.level().playSound(
            null, player.blockPosition(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
            1.0f, 1.5f
        )

        player.setData(ModQuest.QUEST_DATA.get(), data)
        syncToClient(player)
    }

    private fun giveHuntersPot(player: ServerPlayer, count: Int) {
        val stack = ItemStack(EstherServerMod.HUNTERS_POT.get(), count)
        if (!player.inventory.add(stack)) {
            player.drop(stack, false)
        }
    }

    fun resetQuests(player: ServerPlayer, daily: Boolean, weekly: Boolean) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        if (daily) {
            data.dailyResetDay = 0L
        }
        if (weekly) {
            data.weeklyResetDay = 0L
        }
        player.setData(ModQuest.QUEST_DATA.get(), data)
        checkAndResetQuests(player)
    }
}
