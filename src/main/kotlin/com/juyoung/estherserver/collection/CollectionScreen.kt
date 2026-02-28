package com.juyoung.estherserver.collection

import com.juyoung.estherserver.gui.GuiTheme
import net.minecraft.client.gui.GuiGraphics
import com.juyoung.estherserver.sitting.ModKeyBindings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.neoforge.network.PacketDistributor
import java.util.Optional

class CollectionScreen : Screen(Component.translatable("gui.estherserver.collection.title")) {

    companion object {
        private const val GUI_WIDTH = 198
        private const val GUI_HEIGHT = 200
        private const val COLUMNS = 9
        private const val SLOT_SIZE = 18
        private const val PADDING = 9
        private const val TAB_HEIGHT = 14
        private const val TAB_GAP = 2

        private const val GRID_VISIBLE_ROWS = 6

        private const val MILESTONE_ROW_HEIGHT = 28
        private const val MILESTONE_PADDING = 4
    }

    private data class TabEntry(
        val category: CollectionCategory?,
        val translationKey: String,
        val isRewardTab: Boolean = false
    )

    // 2줄 탭: 1줄 - 전체/어종/작물/광물/요리, 2줄 - 블록/장비/음식/소재/보상
    private val tabRow1 = listOf(
        TabEntry(null, "gui.estherserver.collection.tab.all"),
        TabEntry(CollectionCategory.FISH, CollectionCategory.FISH.translationKey),
        TabEntry(CollectionCategory.CROPS, CollectionCategory.CROPS.translationKey),
        TabEntry(CollectionCategory.MINERALS, CollectionCategory.MINERALS.translationKey),
        TabEntry(CollectionCategory.COOKING, CollectionCategory.COOKING.translationKey)
    )
    private val tabRow2 = listOf(
        TabEntry(CollectionCategory.BLOCKS, CollectionCategory.BLOCKS.translationKey),
        TabEntry(CollectionCategory.EQUIPMENT, CollectionCategory.EQUIPMENT.translationKey),
        TabEntry(CollectionCategory.FOOD, CollectionCategory.FOOD.translationKey),
        TabEntry(CollectionCategory.MATERIALS, CollectionCategory.MATERIALS.translationKey),
        TabEntry(null, "gui.estherserver.collection.tab.rewards", isRewardTab = true)
    )

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedCategory: CollectionCategory? = null
    private var showRewardTab = false
    private var gridScroll = 0
    private var rewardScroll = 0
    private val itemCache = mutableMapOf<CollectionKey, ItemStack>()

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        buildItemCache()
    }

    private fun buildItemCache() {
        itemCache.clear()
        for (def in CollectibleRegistry.getAllDefinitions()) {
            val item = BuiltInRegistries.ITEM.getValue(def.key.item)
            if (item !== Items.AIR) {
                itemCache[def.key] = ItemStack(item)
            }
        }
    }

    /** 그리드 등 콘텐츠의 시작 Y (탭 2줄 아래) */
    private fun getContentTop(): Int = guiTop + 19 + TAB_HEIGHT * 2 + TAB_GAP + 4

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderTabs(guiGraphics, mouseX, mouseY)
        if (showRewardTab) {
            renderRewards(guiGraphics, mouseX, mouseY)
        } else {
            renderGrid(guiGraphics, mouseX, mouseY)
            renderProgress(guiGraphics)
            renderTooltips(guiGraphics, mouseX, mouseY)
        }
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)
    }

    private fun renderTitle(guiGraphics: GuiGraphics) {
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.collection.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        renderTabRow(guiGraphics, mouseX, mouseY, tabRow1, guiTop + 19)
        renderTabRow(guiGraphics, mouseX, mouseY, tabRow2, guiTop + 19 + TAB_HEIGHT + TAB_GAP)
    }

    private fun renderTabRow(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, tabs: List<TabEntry>, tabY: Int) {
        var tabX = guiLeft + PADDING

        for (tab in tabs) {
            val label = Component.translatable(tab.translationKey)
            val tabWidth = font.width(label) + 8
            val isSelected = if (tab.isRewardTab) showRewardTab else (!showRewardTab && selectedCategory == tab.category && !tab.isRewardTab)
            val isHovered = mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT

            val bgColor = when {
                isSelected -> GuiTheme.TAB_ACTIVE
                isHovered -> GuiTheme.TAB_HOVER
                else -> GuiTheme.TAB_INACTIVE
            }
            guiGraphics.fill(tabX, tabY, tabX + tabWidth, tabY + TAB_HEIGHT, bgColor)
            guiGraphics.drawString(
                font, label,
                tabX + 4, tabY + 3,
                if (isSelected) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_BODY
            )

            tabX += tabWidth + TAB_GAP
        }
    }

    // ─── Grid (Item Display) ───

    private fun getGridMaxScroll(): Int {
        val defs = getVisibleDefinitions()
        val totalRows = (defs.size + COLUMNS - 1) / COLUMNS
        return (totalRows - GRID_VISIBLE_ROWS).coerceAtLeast(0)
    }

    private fun renderGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val defs = getVisibleDefinitions()
        val data = CollectionClientHandler.cachedData
        val startX = guiLeft + PADDING
        val startY = getContentTop()
        val visibleHeight = GRID_VISIBLE_ROWS * SLOT_SIZE

        val maxScroll = getGridMaxScroll()
        gridScroll = gridScroll.coerceIn(0, maxScroll)

        guiGraphics.enableScissor(
            guiLeft + PADDING, startY,
            guiLeft + GUI_WIDTH - PADDING, startY + visibleHeight
        )

        for ((index, def) in defs.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val slotX = startX + col * SLOT_SIZE
            val slotY = startY + (row - gridScroll) * SLOT_SIZE

            if (slotY + SLOT_SIZE <= startY || slotY >= startY + visibleHeight) continue

            val isDiscovered = data.isComplete(def.key)
            val stack = itemCache[def.key]

            if (isDiscovered) {
                // 등록 아이템: 밝은 시안 테두리 + 아이콘 + 체크마크
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, GuiTheme.BAR_FILL_BRIGHT)
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, GuiTheme.SLOT_INNER)
                if (stack != null) {
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1)
                }
                // 좌상단 초록 체크마크
                guiGraphics.drawString(font, "\u2713", slotX + 1, slotY, 0xFF55FF55.toInt(), true)
            } else {
                // 미등록 아이템: 실제 아이콘 + 어두운 오버레이
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, GuiTheme.PANEL_BORDER_DARK)
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, GuiTheme.SLOT_INNER)
                if (stack != null) {
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1)
                    // 반투명 어두운 오버레이
                    guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xBB1A1A2A.toInt())
                }
            }
        }

        guiGraphics.disableScissor()

        // Scrollbar
        if (maxScroll > 0) {
            val totalRows = (defs.size + COLUMNS - 1) / COLUMNS
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (GRID_VISIBLE_ROWS.toFloat() / totalRows * visibleHeight).toInt().coerceAtLeast(8)
            val scrollbarY = startY + (gridScroll.toFloat() / maxScroll * (visibleHeight - scrollbarHeight)).toInt()
            guiGraphics.fill(scrollbarX, startY, scrollbarX + 3, startY + visibleHeight, GuiTheme.SCROLLBAR_BG)
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, GuiTheme.SCROLLBAR_THUMB)
        }
    }

    private fun renderProgress(guiGraphics: GuiGraphics) {
        val data = CollectionClientHandler.cachedData
        val startY = getContentTop()
        val progressY = startY + GRID_VISIBLE_ROWS * SLOT_SIZE + 4

        val total: Int
        val completed: Int
        if (selectedCategory != null) {
            val defs = CollectibleRegistry.getDefinitionsByCategory(selectedCategory!!)
            total = defs.size
            completed = data.getCompletedCountByCategory(selectedCategory!!)
        } else {
            total = CollectibleRegistry.getTotalCount()
            completed = data.getCompletedCount()
        }
        val percentage = if (total > 0) completed * 100 / total else 0

        val barX = guiLeft + PADDING
        val barWidth = COLUMNS * SLOT_SIZE
        guiGraphics.fill(barX, progressY, barX + barWidth, progressY + 10, GuiTheme.BAR_BG)

        val fillWidth = if (total > 0) barWidth * completed / total else 0
        guiGraphics.fill(barX, progressY, barX + fillWidth, progressY + 10, GuiTheme.BAR_FILL)

        val progressText = Component.translatable(
            "gui.estherserver.collection.progress", completed, total, percentage
        )
        guiGraphics.drawCenteredString(font, progressText, guiLeft + GUI_WIDTH / 2, progressY + 14, GuiTheme.TEXT_WHITE)
    }

    private fun renderTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val defs = getVisibleDefinitions()
        val data = CollectionClientHandler.cachedData
        val startX = guiLeft + PADDING
        val startY = getContentTop()
        val visibleHeight = GRID_VISIBLE_ROWS * SLOT_SIZE

        if (mouseY < startY || mouseY >= startY + visibleHeight) return

        for ((index, def) in defs.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val slotX = startX + col * SLOT_SIZE
            val slotY = startY + (row - gridScroll) * SLOT_SIZE

            if (slotY + SLOT_SIZE <= startY || slotY >= startY + visibleHeight) continue

            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE
            ) {
                val stack = itemCache[def.key]
                if (stack != null) {
                    guiGraphics.renderTooltip(font, listOf(stack.hoverName), Optional.empty(), mouseX, mouseY)
                }
                break
            }
        }
    }

    // ─── Rewards Tab ───

    private fun getRewardVisibleTop(): Int = getContentTop()
    private fun getRewardVisibleHeight(): Int = guiTop + GUI_HEIGHT - 8 - getRewardVisibleTop()
    private fun getRewardTotalHeight(): Int = Milestone.entries.size * MILESTONE_ROW_HEIGHT + MILESTONE_PADDING * 2
    private fun getRewardMaxScroll(): Int = (getRewardTotalHeight() - getRewardVisibleHeight()).coerceAtLeast(0)

    private fun renderRewards(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val data = CollectionClientHandler.cachedData
        val startX = guiLeft + PADDING
        val visibleTop = getRewardVisibleTop()
        val visibleHeight = getRewardVisibleHeight()
        val contentWidth = GUI_WIDTH - PADDING * 2

        rewardScroll = rewardScroll.coerceIn(0, getRewardMaxScroll())

        guiGraphics.enableScissor(
            guiLeft + PADDING, visibleTop,
            guiLeft + GUI_WIDTH - PADDING, visibleTop + visibleHeight
        )

        val startY = visibleTop + MILESTONE_PADDING - rewardScroll

        for ((index, milestone) in Milestone.entries.withIndex()) {
            val rowY = startY + index * MILESTONE_ROW_HEIGHT
            if (rowY + MILESTONE_ROW_HEIGHT <= visibleTop || rowY >= visibleTop + visibleHeight) continue

            val achieved = milestone.check(data)
            val claimed = milestone.id in data.claimedRewards

            val rowBg = when {
                achieved && claimed -> GuiTheme.MILESTONE_UNLOCKED
                achieved -> GuiTheme.MILESTONE_ACTIVE
                else -> GuiTheme.MILESTONE_LOCKED
            }
            guiGraphics.fill(startX, rowY, startX + contentWidth, rowY + MILESTONE_ROW_HEIGHT - 2, rowBg)

            // 마일스톤 이름
            val titleName = Component.translatable(milestone.titleKey)
            val titleColor = if (achieved) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_MUTED
            guiGraphics.drawString(font, titleName, startX + 4, rowY + 3, titleColor)

            // 보상 내용 (2행)
            val reward = milestone.reward
            var rewardX = startX + 4
            val rewardY = rowY + 14

            // 화폐 보상
            if (reward.currencyReward > 0) {
                val currencyText = Component.translatable(
                    "gui.estherserver.collection.reward.currency",
                    reward.currencyReward
                )
                guiGraphics.drawString(font, currencyText, rewardX, rewardY, 0xFFFFD700.toInt())
                rewardX += font.width(currencyText) + 4
            }

            // 아이템 보상 아이콘
            for (itemReward in reward.items) {
                guiGraphics.renderItem(itemReward, rewardX, rewardY - 4)
                rewardX += 18
            }

            // 수령 버튼 / 상태 (우측)
            val buttonWidth = 36
            val buttonX = startX + contentWidth - buttonWidth - 4
            val buttonY = rowY + 4
            val buttonH = MILESTONE_ROW_HEIGHT - 10

            if (achieved && !claimed) {
                // 수령 버튼
                val isHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                    mouseY >= buttonY && mouseY < buttonY + buttonH &&
                    mouseY >= visibleTop && mouseY < visibleTop + visibleHeight
                val btnColor = if (isHovered) GuiTheme.BUTTON_HOVER else GuiTheme.BUTTON
                guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonH, btnColor)
                val claimText = Component.translatable("gui.estherserver.collection.reward.claim")
                guiGraphics.drawCenteredString(font, claimText, buttonX + buttonWidth / 2, buttonY + 3, GuiTheme.TEXT_WHITE)
            } else if (achieved && claimed) {
                val doneText = Component.translatable("gui.estherserver.collection.reward.claimed")
                guiGraphics.drawString(font, doneText, buttonX, buttonY + 3, GuiTheme.TEXT_MUTED)
            } else {
                // 미달성: 진행률
                val progress = milestone.progressProvider?.invoke(data)
                if (progress != null) {
                    val progressText = Component.translatable("gui.estherserver.collection.progress_fraction", progress.first, progress.second)
                    guiGraphics.drawString(font, progressText, buttonX, buttonY + 3, GuiTheme.TEXT_MUTED)
                }
            }
        }

        guiGraphics.disableScissor()

        // Scrollbar
        val maxScroll = getRewardMaxScroll()
        if (maxScroll > 0) {
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (visibleHeight.toFloat() / getRewardTotalHeight() * visibleHeight).toInt().coerceAtLeast(8)
            val scrollbarY = visibleTop + (rewardScroll.toFloat() / maxScroll * (visibleHeight - scrollbarHeight)).toInt()
            guiGraphics.fill(scrollbarX, visibleTop, scrollbarX + 3, visibleTop + visibleHeight, GuiTheme.SCROLLBAR_BG)
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, GuiTheme.SCROLLBAR_THUMB)
        }
    }

    // ─── Interaction ───

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            // 탭 클릭 처리 (2줄)
            if (handleTabClick(mouseX, mouseY, tabRow1, guiTop + 19)) return true
            if (handleTabClick(mouseX, mouseY, tabRow2, guiTop + 19 + TAB_HEIGHT + TAB_GAP)) return true

            // 보상 탭 클릭 (수령 버튼)
            if (showRewardTab) {
                val data = CollectionClientHandler.cachedData
                val startX = guiLeft + PADDING
                val visibleTop = getRewardVisibleTop()
                val visibleHeight = getRewardVisibleHeight()
                val contentWidth = GUI_WIDTH - PADDING * 2
                val startY = visibleTop + MILESTONE_PADDING - rewardScroll
                val buttonWidth = 36

                if (mouseY >= visibleTop && mouseY < visibleTop + visibleHeight) {
                    for ((index, milestone) in Milestone.entries.withIndex()) {
                        val rowY = startY + index * MILESTONE_ROW_HEIGHT
                        val buttonX = startX + contentWidth - buttonWidth - 4
                        val buttonY = rowY + 4
                        val buttonH = MILESTONE_ROW_HEIGHT - 10

                        val achieved = milestone.check(data)
                        val claimed = milestone.id in data.claimedRewards

                        if (achieved && !claimed &&
                            mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                            mouseY >= buttonY && mouseY < buttonY + buttonH
                        ) {
                            PacketDistributor.sendToServer(RewardClaimPayload(milestone.id))
                            return true
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun handleTabClick(mouseX: Double, mouseY: Double, tabs: List<TabEntry>, tabY: Int): Boolean {
        var tabX = guiLeft + PADDING

        for (tab in tabs) {
            val label = Component.translatable(tab.translationKey)
            val tabWidth = font.width(label) + 8

            if (mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
            ) {
                if (tab.isRewardTab) {
                    showRewardTab = true
                    selectedCategory = null
                } else {
                    showRewardTab = false
                    selectedCategory = tab.category
                    gridScroll = 0
                }
                return true
            }
            tabX += tabWidth + TAB_GAP
        }
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (showRewardTab) {
            rewardScroll = (rewardScroll - (scrollY * 12).toInt()).coerceIn(0, getRewardMaxScroll())
            return true
        }
        val maxScroll = getGridMaxScroll()
        if (maxScroll > 0) {
            gridScroll = (gridScroll - scrollY.toInt()).coerceIn(0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ModKeyBindings.COLLECTION_KEY.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean = false

    private fun getVisibleDefinitions(): List<CollectibleDefinition> {
        val category = selectedCategory ?: return CollectibleRegistry.getAllDefinitions()
        return CollectibleRegistry.getDefinitionsByCategory(category)
    }
}
