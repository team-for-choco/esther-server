package com.juyoung.estherserver.quest

import com.juyoung.estherserver.gui.GuiTheme
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.PacketDistributor

class QuestScreen : Screen(Component.translatable("gui.estherserver.quest.title")) {

    companion object {
        private const val GUI_WIDTH = 260
        private const val GUI_HEIGHT = 240
        private const val TAB_WIDTH = 60
        private const val TAB_HEIGHT = 16
        private const val QUEST_ROW_HEIGHT = 36
        private const val CLAIM_BUTTON_W = 36
        private const val CLAIM_BUTTON_H = 14
        private const val BONUS_AREA_HEIGHT = 32
    }

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedTab = 0 // 0=daily, 1=weekly

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        val data = QuestClientHandler.cachedData

        // Background panel
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // Title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.quest.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )

        // Tabs
        renderTabs(guiGraphics, mouseX, mouseY)

        val contentY = guiTop + 36
        val contentX = guiLeft + 8
        val contentW = GUI_WIDTH - 16

        val questList = if (selectedTab == 0) data.dailyQuests else data.weeklyQuests
        val claimedCount = if (selectedTab == 0) data.getDailyClaimedCount() else data.getWeeklyClaimedCount()
        val bonusClaimed = if (selectedTab == 0) data.dailyBonusClaimed else data.weeklyBonusClaimed

        // Quest list area
        GuiTheme.renderInnerPanel(guiGraphics, contentX, contentY, contentW, QUEST_ROW_HEIGHT * 5 + 4)

        for ((i, quest) in questList.withIndex()) {
            renderQuestRow(guiGraphics, contentX + 2, contentY + 2 + i * QUEST_ROW_HEIGHT, contentW - 4, quest, i, mouseX, mouseY)
        }

        // Bonus area
        val bonusY = contentY + QUEST_ROW_HEIGHT * 5 + 8
        renderBonusArea(guiGraphics, contentX, bonusY, contentW, claimedCount, bonusClaimed, mouseX, mouseY)
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val tabY = guiTop + 20
        val data = QuestClientHandler.cachedData

        for (i in 0..1) {
            val tabX = guiLeft + 8 + i * (TAB_WIDTH + 4)
            val isActive = selectedTab == i
            val isHover = mouseX in tabX until tabX + TAB_WIDTH && mouseY in tabY until tabY + TAB_HEIGHT

            val color = when {
                isActive -> GuiTheme.TAB_ACTIVE
                isHover -> GuiTheme.TAB_HOVER
                else -> GuiTheme.TAB_INACTIVE
            }
            guiGraphics.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, color)

            val claimedCount = if (i == 0) data.getDailyClaimedCount() else data.getWeeklyClaimedCount()
            val tabKey = if (i == 0) "gui.estherserver.quest.tab_daily" else "gui.estherserver.quest.tab_weekly"
            val label = Component.translatable(tabKey).append(" $claimedCount/3")
            guiGraphics.drawCenteredString(
                font, label,
                tabX + TAB_WIDTH / 2, tabY + 4,
                if (isActive) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_MUTED
            )
        }
    }

    private fun renderQuestRow(
        guiGraphics: GuiGraphics, x: Int, y: Int, w: Int,
        quest: ActiveQuest, index: Int,
        mouseX: Int, mouseY: Int
    ) {
        val template = QuestPool.getTemplate(quest.templateId) ?: return

        // Row background
        val rowBg = if (quest.claimed) GuiTheme.ROW_SELECTED else GuiTheme.ROW_BG
        guiGraphics.fill(x, y, x + w, y + QUEST_ROW_HEIGHT - 2, rowBg)

        // Quest name
        val questName = Component.translatable(template.translationKey)
        guiGraphics.drawString(font, questName, x + 4, y + 3, GuiTheme.TEXT_BODY, false)

        // Progress bar
        val barX = x + 4
        val barY = y + 14
        val barW = w - CLAIM_BUTTON_W - 12
        val barH = 8
        val progress = quest.progress.toFloat() / template.targetCount.toFloat()

        guiGraphics.fill(barX, barY, barX + barW, barY + barH, GuiTheme.BAR_BG)
        val fillW = (barW * progress.coerceIn(0f, 1f)).toInt()
        if (fillW > 0) {
            val barColor = if (quest.isComplete(template)) GuiTheme.BAR_FILL_BRIGHT else GuiTheme.BAR_FILL
            guiGraphics.fill(barX, barY, barX + fillW, barY + barH, barColor)
        }

        // Progress text
        val progressText = "${quest.progress}/${template.targetCount}"
        guiGraphics.drawCenteredString(font, progressText, barX + barW / 2, barY, GuiTheme.TEXT_WHITE)

        // Reward hint
        val rewardText = Component.literal("${template.baseCurrencyReward}G")
        guiGraphics.drawString(font, rewardText, x + 4, y + 24, GuiTheme.TEXT_GOLD, false)

        // Claim button
        val btnX = x + w - CLAIM_BUTTON_W - 4
        val btnY = y + (QUEST_ROW_HEIGHT - 2 - CLAIM_BUTTON_H) / 2
        val isComplete = quest.isComplete(template)
        val canClaim = isComplete && !quest.claimed
        val data = QuestClientHandler.cachedData
        val claimedCount = if (selectedTab == 0) data.getDailyClaimedCount() else data.getWeeklyClaimedCount()
        val maxReached = claimedCount >= 3

        val btnColor = when {
            quest.claimed -> GuiTheme.BUTTON_DISABLED
            canClaim && !maxReached -> {
                val isHover = mouseX in btnX until btnX + CLAIM_BUTTON_W && mouseY in btnY until btnY + CLAIM_BUTTON_H
                if (isHover) GuiTheme.BUTTON_HOVER else GuiTheme.BUTTON
            }
            else -> GuiTheme.BUTTON_DISABLED
        }
        guiGraphics.fill(btnX, btnY, btnX + CLAIM_BUTTON_W, btnY + CLAIM_BUTTON_H, btnColor)

        val btnText = when {
            quest.claimed -> Component.translatable("gui.estherserver.quest.claimed")
            else -> Component.translatable("gui.estherserver.quest.claim")
        }
        guiGraphics.drawCenteredString(font, btnText, btnX + CLAIM_BUTTON_W / 2, btnY + 3, GuiTheme.TEXT_WHITE)
    }

    private fun renderBonusArea(
        guiGraphics: GuiGraphics, x: Int, y: Int, w: Int,
        claimedCount: Int, bonusClaimed: Boolean,
        mouseX: Int, mouseY: Int
    ) {
        GuiTheme.renderInnerPanel(guiGraphics, x, y, w, BONUS_AREA_HEIGHT)

        val bonusReady = claimedCount >= 3

        // Bonus label
        val bonusLabel = Component.translatable("gui.estherserver.quest.bonus", claimedCount, 3)
        guiGraphics.drawString(font, bonusLabel, x + 6, y + 4, GuiTheme.TEXT_BODY, false)

        // Bonus reward info
        val currency = if (selectedTab == 1) 200 else 50
        val soupCount = if (selectedTab == 1) 3 else 1
        val rewardInfo = Component.translatable("gui.estherserver.quest.bonus_reward", currency, soupCount)
        guiGraphics.drawString(font, rewardInfo, x + 6, y + 16, GuiTheme.TEXT_GOLD, false)

        // Bonus claim button
        val btnW = 50
        val btnH = 14
        val btnX = x + w - btnW - 6
        val btnY = y + (BONUS_AREA_HEIGHT - btnH) / 2

        val btnColor = when {
            bonusClaimed -> GuiTheme.BUTTON_DISABLED
            bonusReady -> {
                val isHover = mouseX in btnX until btnX + btnW && mouseY in btnY until btnY + btnH
                if (isHover) GuiTheme.BUTTON_HOVER else GuiTheme.BUTTON
            }
            else -> GuiTheme.BUTTON_DISABLED
        }
        guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnColor)

        val btnText = when {
            bonusClaimed -> Component.translatable("gui.estherserver.quest.claimed")
            else -> Component.translatable("gui.estherserver.quest.bonus_claim")
        }
        guiGraphics.drawCenteredString(font, btnText, btnX + btnW / 2, btnY + 3, GuiTheme.TEXT_WHITE)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        // Tab clicks
        val tabY = guiTop + 20
        for (i in 0..1) {
            val tabX = guiLeft + 8 + i * (TAB_WIDTH + 4)
            if (mx in tabX until tabX + TAB_WIDTH && my in tabY until tabY + TAB_HEIGHT) {
                selectedTab = i
                return true
            }
        }

        val contentY = guiTop + 36
        val contentX = guiLeft + 8
        val contentW = GUI_WIDTH - 16
        val data = QuestClientHandler.cachedData
        val questList = if (selectedTab == 0) data.dailyQuests else data.weeklyQuests

        // Quest claim buttons
        for ((i, quest) in questList.withIndex()) {
            val rowX = contentX + 2
            val rowY = contentY + 2 + i * QUEST_ROW_HEIGHT
            val rowW = contentW - 4
            val btnX = rowX + rowW - CLAIM_BUTTON_W - 4
            val btnY = rowY + (QUEST_ROW_HEIGHT - 2 - CLAIM_BUTTON_H) / 2

            if (mx in btnX until btnX + CLAIM_BUTTON_W && my in btnY until btnY + CLAIM_BUTTON_H) {
                val template = QuestPool.getTemplate(quest.templateId) ?: continue
                if (quest.isComplete(template) && !quest.claimed) {
                    PacketDistributor.sendToServer(QuestClaimPayload(i, selectedTab == 1))
                    return true
                }
            }
        }

        // Bonus claim button
        val bonusY = contentY + QUEST_ROW_HEIGHT * 5 + 8
        val btnW = 50
        val btnH = 14
        val btnX = contentX + contentW - btnW - 6
        val btnBY = bonusY + (BONUS_AREA_HEIGHT - btnH) / 2
        if (mx in btnX until btnX + btnW && my in btnBY until btnBY + btnH) {
            val claimedCount = if (selectedTab == 0) data.getDailyClaimedCount() else data.getWeeklyClaimedCount()
            val bonusClaimed = if (selectedTab == 0) data.dailyBonusClaimed else data.weeklyBonusClaimed
            if (claimedCount >= 3 && !bonusClaimed) {
                PacketDistributor.sendToServer(QuestBonusClaimPayload(selectedTab == 1))
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_H) {
            this.minecraft!!.setScreen(null)
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean = false
}
