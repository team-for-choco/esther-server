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
        private const val GUI_HEIGHT = 186
        private const val COLUMNS = 9
        private const val SLOT_SIZE = 18
        private const val PADDING = 9
        private const val TAB_HEIGHT = 16
        private const val TAB_GAP = 2

        private const val MILESTONE_ROW_HEIGHT = 24
        private const val MILESTONE_PADDING = 4

        private const val TITLE_TAB_KEY = "gui.estherserver.collection.tab.title"
    }

    private data class TabEntry(val category: CollectionCategory?, val translationKey: String, val isTitleTab: Boolean = false)

    private val tabs = listOf(
        TabEntry(null, "gui.estherserver.collection.tab.all"),
        TabEntry(CollectionCategory.FISH, CollectionCategory.FISH.translationKey),
        TabEntry(CollectionCategory.CROPS, CollectionCategory.CROPS.translationKey),
        TabEntry(CollectionCategory.MINERALS, CollectionCategory.MINERALS.translationKey),
        TabEntry(CollectionCategory.COOKING, CollectionCategory.COOKING.translationKey),
        TabEntry(null, TITLE_TAB_KEY, isTitleTab = true)
    )

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedCategory: CollectionCategory? = null
    private var showTitleTab = false
    private var milestoneScroll = 0
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

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderTabs(guiGraphics, mouseX, mouseY)
        if (showTitleTab) {
            renderMilestones(guiGraphics, mouseX, mouseY)
        } else {
            renderGrid(guiGraphics, mouseX, mouseY)
            renderProgress(guiGraphics)
            renderTooltips(guiGraphics, mouseX, mouseY)
        }
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        if (!showTitleTab) {
            val gridY = guiTop + 38
            val gridHeight = getVisibleDefinitions().size.let { count ->
                val rows = (count + COLUMNS - 1) / COLUMNS
                rows.coerceAtLeast(1) * SLOT_SIZE + 4
            }
            GuiTheme.renderInnerPanel(
                guiGraphics,
                guiLeft + PADDING - 1, gridY - 1,
                COLUMNS * SLOT_SIZE + 3, gridHeight + 3
            )
        } else {
            val contentY = guiTop + 38
            val visibleHeight = guiTop + GUI_HEIGHT - 8 - contentY
            GuiTheme.renderInnerPanel(
                guiGraphics,
                guiLeft + PADDING - 1, contentY - 1,
                GUI_WIDTH - PADDING * 2 + 2, visibleHeight + 2
            )
        }
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
        var tabX = guiLeft + PADDING
        val tabY = guiTop + 19

        for (tab in tabs) {
            val label = Component.translatable(tab.translationKey)
            val tabWidth = font.width(label) + 8
            val isSelected = if (tab.isTitleTab) showTitleTab else (!showTitleTab && selectedCategory == tab.category)
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
                tabX + 4, tabY + 4,
                if (isSelected) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_BODY
            )

            tabX += tabWidth + TAB_GAP
        }
    }

    private fun getMilestoneVisibleTop(): Int = guiTop + 38
    private fun getMilestoneVisibleHeight(): Int = guiTop + GUI_HEIGHT - 8 - getMilestoneVisibleTop()
    private fun getMilestoneTotalHeight(): Int = Milestone.entries.size * MILESTONE_ROW_HEIGHT + MILESTONE_PADDING * 2
    private fun getMilestoneMaxScroll(): Int = (getMilestoneTotalHeight() - getMilestoneVisibleHeight()).coerceAtLeast(0)

    private fun renderMilestones(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val data = CollectionClientHandler.cachedData
        val startX = guiLeft + PADDING
        val visibleTop = getMilestoneVisibleTop()
        val visibleHeight = getMilestoneVisibleHeight()
        val contentWidth = GUI_WIDTH - PADDING * 2

        milestoneScroll = milestoneScroll.coerceIn(0, getMilestoneMaxScroll())

        guiGraphics.enableScissor(
            guiLeft + PADDING, visibleTop,
            guiLeft + GUI_WIDTH - PADDING, visibleTop + visibleHeight
        )

        val startY = visibleTop + MILESTONE_PADDING - milestoneScroll

        for ((index, milestone) in Milestone.entries.withIndex()) {
            val rowY = startY + index * MILESTONE_ROW_HEIGHT
            val unlocked = milestone.id in data.unlockedMilestones
            val isActive = data.activeTitle == milestone.id
            val isHovered = mouseX >= startX && mouseX < startX + contentWidth &&
                mouseY >= rowY && mouseY < rowY + MILESTONE_ROW_HEIGHT - 2 &&
                mouseY >= visibleTop && mouseY < visibleTop + visibleHeight

            val rowBg = when {
                isActive -> GuiTheme.MILESTONE_ACTIVE
                unlocked && isHovered -> GuiTheme.MILESTONE_UNLOCKED_HOVER
                unlocked -> GuiTheme.MILESTONE_UNLOCKED
                else -> GuiTheme.MILESTONE_LOCKED
            }
            guiGraphics.fill(startX, rowY, startX + contentWidth, rowY + MILESTONE_ROW_HEIGHT - 2, rowBg)

            val starText = if (unlocked) "\u2605" else "\u2606"
            val starColor = if (unlocked) GuiTheme.TEXT_GOLD else GuiTheme.TEXT_MUTED
            guiGraphics.drawString(font, starText, startX + 3, rowY + 3, starColor)

            val titleName = Component.translatable(milestone.titleKey)
            val titleColor = if (unlocked) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_MUTED
            guiGraphics.drawString(font, titleName, startX + 14, rowY + 3, titleColor)

            if (unlocked) {
                val condText = Component.translatable(milestone.descriptionKey)
                guiGraphics.drawString(font, condText, startX + 14, rowY + 13, GuiTheme.TEXT_BODY)
            } else {
                val progress = milestone.progressProvider?.invoke(data)
                val condText = Component.translatable(milestone.descriptionKey)
                if (progress != null) {
                    condText.append(Component.literal(" (${progress.first}/${progress.second})"))
                }
                guiGraphics.drawString(font, condText, startX + 14, rowY + 13, GuiTheme.TEXT_MUTED)
            }

            if (isActive) {
                guiGraphics.drawString(font, "\u2714", startX + contentWidth - 12, rowY + 7, GuiTheme.BAR_FILL_BRIGHT)
            }
        }

        guiGraphics.disableScissor()

        // Scrollbar
        val maxScroll = getMilestoneMaxScroll()
        if (maxScroll > 0) {
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (visibleHeight.toFloat() / getMilestoneTotalHeight() * visibleHeight).toInt().coerceAtLeast(8)
            val scrollbarY = visibleTop + (milestoneScroll.toFloat() / maxScroll * (visibleHeight - scrollbarHeight)).toInt()
            guiGraphics.fill(scrollbarX, visibleTop, scrollbarX + 3, visibleTop + visibleHeight, GuiTheme.SCROLLBAR_BG)
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, GuiTheme.SCROLLBAR_THUMB)
        }
    }

    private fun renderGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val defs = getVisibleDefinitions()
        val data = CollectionClientHandler.cachedData
        val startX = guiLeft + PADDING
        val startY = guiTop + 38

        for ((index, def) in defs.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val slotX = startX + col * SLOT_SIZE
            val slotY = startY + row * SLOT_SIZE

            val isDiscovered = data.isComplete(def.key)

            if (isDiscovered) {
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, GuiTheme.SLOT_BG)
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, GuiTheme.SLOT_INNER)

                val stack = itemCache[def.key]
                if (stack != null) {
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1)
                }
            } else {
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, GuiTheme.PANEL_BORDER_DARK)
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, GuiTheme.UNDISCOVERED_BG)
                guiGraphics.drawCenteredString(font, "?", slotX + 9, slotY + 5, GuiTheme.TEXT_MUTED)
            }
        }
    }

    private fun renderProgress(guiGraphics: GuiGraphics) {
        val data = CollectionClientHandler.cachedData
        val total = CollectibleRegistry.getTotalCount()
        val completed = data.getCompletedCount()
        val percentage = if (total > 0) completed * 100 / total else 0

        val defs = getVisibleDefinitions()
        val rows = (defs.size + COLUMNS - 1) / COLUMNS
        val progressY = guiTop + 38 + rows.coerceAtLeast(1) * SLOT_SIZE + 8

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
        val startY = guiTop + 38

        for ((index, def) in defs.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val slotX = startX + col * SLOT_SIZE
            val slotY = startY + row * SLOT_SIZE

            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE
            ) {
                val isDiscovered = data.isComplete(def.key)
                if (isDiscovered) {
                    val stack = itemCache[def.key]
                    if (stack != null) {
                        guiGraphics.renderTooltip(font, listOf(stack.hoverName), Optional.empty(), mouseX, mouseY)
                    }
                } else {
                    guiGraphics.renderTooltip(
                        font,
                        Component.literal("???"),
                        mouseX, mouseY
                    )
                }
                break
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            var tabX = guiLeft + PADDING
            val tabY = guiTop + 19

            for (tab in tabs) {
                val label = Component.translatable(tab.translationKey)
                val tabWidth = font.width(label) + 8

                if (mouseX >= tabX && mouseX < tabX + tabWidth &&
                    mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
                ) {
                    if (tab.isTitleTab) {
                        showTitleTab = true
                        selectedCategory = null
                    } else {
                        showTitleTab = false
                        selectedCategory = tab.category
                    }
                    return true
                }
                tabX += tabWidth + TAB_GAP
            }

            if (showTitleTab) {
                val data = CollectionClientHandler.cachedData
                val startX = guiLeft + PADDING
                val visibleTop = getMilestoneVisibleTop()
                val visibleHeight = getMilestoneVisibleHeight()
                val startY = visibleTop + MILESTONE_PADDING - milestoneScroll
                val contentWidth = GUI_WIDTH - PADDING * 2

                if (mouseY >= visibleTop && mouseY < visibleTop + visibleHeight) {
                    for ((index, milestone) in Milestone.entries.withIndex()) {
                        val rowY = startY + index * MILESTONE_ROW_HEIGHT
                        if (mouseX >= startX && mouseX < startX + contentWidth &&
                            mouseY >= rowY && mouseY < rowY + MILESTONE_ROW_HEIGHT - 2
                        ) {
                            val unlocked = milestone.id in data.unlockedMilestones
                            if (unlocked) {
                                val newId = if (data.activeTitle == milestone.id) "" else milestone.id
                                PacketDistributor.sendToServer(TitleSelectPayload(newId))
                            }
                            return true
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (showTitleTab) {
            milestoneScroll = (milestoneScroll - (scrollY * 12).toInt()).coerceIn(0, getMilestoneMaxScroll())
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
