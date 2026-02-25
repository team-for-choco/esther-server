package com.juyoung.estherserver.quest

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.wild.WildDimensionKeys
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

object QuestHandler {

    private val LOGGER = LoggerFactory.getLogger("QuestHandler")
    private val KST = ZoneId.of("Asia/Seoul")
    private const val MAX_CLAIMS = 3

    // Daily rewards
    private const val DAILY_CURRENCY = 1000
    private const val DAILY_SOUP = 30
    private const val DAILY_BONUS_CURRENCY = 1500
    private const val DAILY_BONUS_SOUP = 20
    private const val DAILY_BONUS_TICKET = 1

    // Weekly rewards
    private const val WEEKLY_CURRENCY = 5000
    private const val WEEKLY_SOUP = 150
    private const val WEEKLY_BONUS_CURRENCY = 7500
    private const val WEEKLY_BONUS_SOUP = 100
    private const val WEEKLY_BONUS_TICKET = 3

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
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
    fun onLivingDeath(event: LivingDeathEvent) {
        val killed = event.entity
        val source = event.source
        val player = source.entity as? ServerPlayer ?: return

        // Only count kills in the wild dimension
        if (player.level().dimension() != WildDimensionKeys.WILD_LEVEL) return

        val entityTypeKey = BuiltInRegistries.ENTITY_TYPE.getKey(killed.type)
        val entityTypeId = entityTypeKey.toString()

        onMobKilled(player, entityTypeId)
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        PacketDistributor.sendToPlayer(player, QuestSyncPayload(data))
    }

    private fun getDayNumber(instant: Instant): Long {
        val kstTime = ZonedDateTime.ofInstant(instant, KST)
        return kstTime.toLocalDate().toEpochDay()
    }

    private fun getWeekStartDay(instant: Instant): Long {
        val kstTime = ZonedDateTime.ofInstant(instant, KST)
        val dayOfWeek = kstTime.get(ChronoField.DAY_OF_WEEK) // 1=Monday
        return kstTime.toLocalDate().toEpochDay() - (dayOfWeek - 1)
    }

    /**
     * Main interaction entry point — called when player interacts with the quest board.
     */
    fun handleBoardInteraction(player: ServerPlayer, hand: InteractionHand, heldStack: ItemStack): InteractionResult {
        LOGGER.info("[QUEST] handleBoardInteraction called - player={}, hand={}, heldEmpty={}", player.name.string, hand, heldStack.isEmpty)

        val data = player.getData(ModQuest.QUEST_DATA.get())
        val now = Instant.now()
        val currentDay = getDayNumber(now)
        val currentWeekStart = getWeekStartDay(now)

        LOGGER.info("[QUEST] data BEFORE: daily={}, weekly={}, dailyResetDay={}, currentDay={}, weeklyResetDay={}, currentWeekStart={}",
            data.dailyQuests.size, data.weeklyQuests.size, data.dailyResetDay, currentDay, data.weeklyResetDay, currentWeekStart)

        // Daily reset check
        if (data.dailyResetDay != currentDay) {
            data.dailyQuests.clear()
            data.dailyBonusClaimed = false
            data.dailyResetDay = currentDay
            LOGGER.info("[QUEST] Daily reset triggered")
        }

        // Weekly reset check
        if (data.weeklyResetDay != currentWeekStart) {
            data.weeklyQuests.clear()
            data.weeklyBonusClaimed = false
            data.weeklyResetDay = currentWeekStart
            LOGGER.info("[QUEST] Weekly reset triggered")
        }

        // Daily assignment if empty
        if (data.dailyQuests.isEmpty()) {
            val seed = player.uuid.hashCode().toLong() xor (currentDay * 31)
            val templates = QuestPool.selectDailyQuests(seed)
            LOGGER.info("[QUEST] Assigning {} daily quests", templates.size)
            for (template in templates) {
                data.dailyQuests.add(ActiveQuest(template.id))
            }
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_daily_assigned"), false
            )
        }

        // Weekly assignment if empty
        if (data.weeklyQuests.isEmpty()) {
            val templates = QuestPool.getWeeklyQuests()
            LOGGER.info("[QUEST] Assigning {} weekly quests", templates.size)
            for (template in templates) {
                data.weeklyQuests.add(ActiveQuest(template.id))
            }
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_weekly_assigned"), false
            )
        }

        LOGGER.info("[QUEST] data AFTER: daily={}, weekly={}", data.dailyQuests.size, data.weeklyQuests.size)

        // Always save and sync to ensure client has up-to-date data
        player.setData(ModQuest.QUEST_DATA.get(), data)
        syncToClient(player)

        // If holding an item, try to submit it
        if (!heldStack.isEmpty && hand == InteractionHand.MAIN_HAND) {
            LOGGER.info("[QUEST] Attempting item submission")
            return handleItemSubmission(player, heldStack, data)
        }

        // Empty hand — open GUI (include data in payload to eliminate timing issues)
        LOGGER.info("[QUEST] Opening quest screen")
        PacketDistributor.sendToPlayer(player, QuestOpenScreenPayload(data))
        return InteractionResult.SUCCESS
    }

    private fun handleItemSubmission(player: ServerPlayer, heldStack: ItemStack, data: QuestData): InteractionResult {
        val itemKey = heldStack.itemHolder.unwrapKey().orElse(null)?.location() ?: return InteractionResult.PASS
        val itemId = itemKey.toString()
        var anySubmitted = false

        // Try daily SUBMIT_ITEM quests
        for (quest in data.dailyQuests) {
            if (quest.claimed) continue
            val template = QuestPool.getTemplate(quest.templateId) ?: continue
            if (template.trackingType != QuestTrackingType.SUBMIT_ITEM) continue
            if (quest.progress >= template.targetCount) continue

            val matches = if (template.targetItemId != null) {
                itemId == template.targetItemId
            } else {
                isItemInCategory(itemKey, template.category)
            }

            if (matches) {
                val needed = template.targetCount - quest.progress
                val toConsume = needed.coerceAtMost(heldStack.count)
                heldStack.shrink(toConsume)
                quest.progress += toConsume
                anySubmitted = true

                // Also count toward weekly quests of the same category
                incrementWeeklyProgress(data, template.category, QuestTrackingType.SUBMIT_ITEM, toConsume, itemKey)

                if (heldStack.isEmpty) break
            }
        }

        // If nothing matched daily, try weekly-only submission
        if (!anySubmitted) {
            for (quest in data.weeklyQuests) {
                if (quest.claimed) continue
                val template = QuestPool.getTemplate(quest.templateId) ?: continue
                if (template.trackingType != QuestTrackingType.SUBMIT_ITEM) continue
                if (quest.progress >= template.targetCount) continue

                val matches = if (template.targetItemId != null) {
                    itemId == template.targetItemId
                } else {
                    isItemInCategory(itemKey, template.category)
                }

                if (matches) {
                    val needed = template.targetCount - quest.progress
                    val toConsume = needed.coerceAtMost(heldStack.count)
                    heldStack.shrink(toConsume)
                    quest.progress += toConsume
                    anySubmitted = true
                    if (heldStack.isEmpty) break
                }
            }
        }

        if (anySubmitted) {
            player.setData(ModQuest.QUEST_DATA.get(), data)
            syncToClient(player)
            player.displayClientMessage(
                Component.translatable("message.estherserver.quest_submitted"), true
            )
            player.level().playSound(
                null, player.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                0.5f, 1.2f
            )
            return InteractionResult.SUCCESS
        }

        player.displayClientMessage(
            Component.translatable("message.estherserver.quest_no_matching"), true
        )
        return InteractionResult.SUCCESS
    }

    private fun incrementWeeklyProgress(data: QuestData, category: QuestCategory, trackingType: QuestTrackingType, amount: Int, itemKey: ResourceLocation) {
        for (quest in data.weeklyQuests) {
            if (quest.claimed) continue
            val template = QuestPool.getTemplate(quest.templateId) ?: continue
            if (template.trackingType != trackingType) continue
            if (template.category != category) continue
            if (quest.progress >= template.targetCount) continue

            val matches = if (template.targetItemId != null) {
                itemKey.toString() == template.targetItemId
            } else {
                isItemInCategory(itemKey, template.category)
            }

            if (matches) {
                val canAdd = (template.targetCount - quest.progress).coerceAtMost(amount)
                quest.progress += canAdd
            }
        }
    }

    fun onMobKilled(player: ServerPlayer, entityTypeId: String) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        var changed = false

        // Daily KILL_MONSTER quests
        for (quest in data.dailyQuests) {
            if (quest.claimed) continue
            val template = QuestPool.getTemplate(quest.templateId) ?: continue
            if (template.trackingType != QuestTrackingType.KILL_MONSTER) continue
            if (quest.progress >= template.targetCount) continue

            val targets = template.targetEntityTypes ?: continue
            if (entityTypeId in targets || isZombieVariant(entityTypeId, targets)) {
                quest.progress = (quest.progress + 1).coerceAtMost(template.targetCount)
                changed = true

                // Also count toward weekly KILL_MONSTER
                for (wQuest in data.weeklyQuests) {
                    if (wQuest.claimed) continue
                    val wTemplate = QuestPool.getTemplate(wQuest.templateId) ?: continue
                    if (wTemplate.trackingType != QuestTrackingType.KILL_MONSTER) continue
                    if (wQuest.progress >= wTemplate.targetCount) continue
                    val wTargets = wTemplate.targetEntityTypes ?: continue
                    if (entityTypeId in wTargets || isZombieVariant(entityTypeId, wTargets)) {
                        wQuest.progress = (wQuest.progress + 1).coerceAtMost(wTemplate.targetCount)
                    }
                }
            }
        }

        // Weekly-only kill matching (if no daily matched)
        if (!changed) {
            for (quest in data.weeklyQuests) {
                if (quest.claimed) continue
                val template = QuestPool.getTemplate(quest.templateId) ?: continue
                if (template.trackingType != QuestTrackingType.KILL_MONSTER) continue
                if (quest.progress >= template.targetCount) continue
                val targets = template.targetEntityTypes ?: continue
                if (entityTypeId in targets || isZombieVariant(entityTypeId, targets)) {
                    quest.progress = (quest.progress + 1).coerceAtMost(template.targetCount)
                    changed = true
                }
            }
        }

        if (changed) {
            player.setData(ModQuest.QUEST_DATA.get(), data)
            syncToClient(player)
        }
    }

    /**
     * Baby zombies and zombie variants count as "minecraft:zombie".
     */
    private fun isZombieVariant(entityTypeId: String, targets: List<String>): Boolean {
        if ("minecraft:zombie" !in targets) return false
        return entityTypeId == "minecraft:zombie_villager" ||
               entityTypeId == "minecraft:husk" ||
               entityTypeId == "minecraft:drowned"
    }

    private fun isItemInCategory(itemId: ResourceLocation, category: QuestCategory): Boolean {
        return when (category) {
            QuestCategory.FISHING -> ProfessionBonusHelper.getFishGrade(itemId) != null
            QuestCategory.FARMING -> ProfessionBonusHelper.getCropGrade(itemId) != null
            QuestCategory.MINING -> ProfessionBonusHelper.getOreGrade(itemId) != null
            QuestCategory.COOKING -> ProfessionBonusHelper.getRecipeGrade(itemId) != null
            QuestCategory.GENERAL -> false
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

        // Give rewards from template
        val currency = template.currencyReward
        val soup = template.huntersPotReward

        EconomyHandler.addBalance(player, currency.toLong())
        giveHuntersPot(player, soup)

        player.displayClientMessage(
            Component.translatable("message.estherserver.quest_claimed", currency, soup), false
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
        val soupCount = if (isWeekly) WEEKLY_BONUS_SOUP else DAILY_BONUS_SOUP
        val ticketCount = if (isWeekly) WEEKLY_BONUS_TICKET else DAILY_BONUS_TICKET

        if (isWeekly) data.weeklyBonusClaimed = true else data.dailyBonusClaimed = true

        EconomyHandler.addBalance(player, currency.toLong())
        giveHuntersPot(player, soupCount)
        giveDrawTicket(player, ticketCount)

        player.displayClientMessage(
            Component.translatable("message.estherserver.quest_bonus_claimed", currency, soupCount, ticketCount), false
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

    private fun giveDrawTicket(player: ServerPlayer, count: Int) {
        val stack = ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), count)
        if (!player.inventory.add(stack)) {
            player.drop(stack, false)
        }
    }

    fun resetQuests(player: ServerPlayer, daily: Boolean, weekly: Boolean) {
        val data = player.getData(ModQuest.QUEST_DATA.get())
        if (daily) {
            data.dailyQuests.clear()
            data.dailyResetDay = 0L
            data.dailyBonusClaimed = false
        }
        if (weekly) {
            data.weeklyQuests.clear()
            data.weeklyResetDay = 0L
            data.weeklyBonusClaimed = false
        }
        player.setData(ModQuest.QUEST_DATA.get(), data)
        syncToClient(player)
    }
}
