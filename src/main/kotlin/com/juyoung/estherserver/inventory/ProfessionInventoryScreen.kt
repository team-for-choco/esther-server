package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionClientHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ProfessionInventoryScreen : Screen(Component.translatable("gui.estherserver.prof_inventory.title")) {

    companion object {
        private const val GUI_WIDTH = 280
        private const val GUI_HEIGHT = 240
        private const val PADDING = 8
        private const val TAB_WIDTH = 50
        private const val TAB_HEIGHT = 18
        private const val SLOT_SIZE = 18
        private const val SLOTS_PER_ROW = 5

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val TAB_ACTIVE = 0xFFAAAAAA.toInt()
        private val TAB_INACTIVE = 0xFF888888.toInt()
        private val SLOT_BG = 0xFF8B8B8B.toInt()
        private val SLOT_LOCKED = 0xFF555555.toInt()
        private val SLOT_EMPTY = 0xFF6B6B6B.toInt()
        private val TEXT_COLOR = 0xFF404040.toInt()
        private val TEXT_LIGHT = 0xFFCCCCCC.toInt()

        private val TABS = listOf(
            "gui.estherserver.prof_inventory.tab.general",
            "profession.estherserver.mining",
            "profession.estherserver.fishing",
            "profession.estherserver.farming",
            "profession.estherserver.cooking"
        )

        private val TAB_PROFESSIONS = listOf(
            null,                // General tab
            Profession.MINING,
            Profession.FISHING,
            Profession.FARMING,
            Profession.COOKING
        )
    }

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedTab = 0

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
    }

    fun refreshData() {
        // Called when sync data arrives
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Background panel
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())

        // Title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.prof_inventory.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 4,
            TEXT_COLOR
        )

        // Tabs
        renderTabs(guiGraphics, mouseX, mouseY)

        // Content based on selected tab
        val contentY = guiTop + 24 + TAB_HEIGHT
        if (selectedTab == 0) {
            renderGeneralTab(guiGraphics, contentY)
        } else {
            val profession = TAB_PROFESSIONS[selectedTab]
            if (profession != null) {
                renderProfessionTab(guiGraphics, contentY, profession)
            }
        }
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val tabY = guiTop + 18
        for (i in TABS.indices) {
            val tabX = guiLeft + PADDING + i * (TAB_WIDTH + 2)
            val isActive = i == selectedTab
            val color = if (isActive) TAB_ACTIVE else TAB_INACTIVE

            guiGraphics.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, color)
            guiGraphics.renderOutline(tabX, tabY, TAB_WIDTH, TAB_HEIGHT, 0xFF000000.toInt())

            val label = Component.translatable(TABS[i])
            val labelWidth = font.width(label)
            guiGraphics.drawString(
                font,
                label,
                tabX + (TAB_WIDTH - labelWidth) / 2,
                tabY + 5,
                if (isActive) TEXT_COLOR else TEXT_LIGHT
            )
        }
    }

    private fun renderGeneralTab(guiGraphics: GuiGraphics, startY: Int) {
        val profData = ProfessionClientHandler.cachedData
        val x = guiLeft + PADDING
        var y = startY + 4

        guiGraphics.drawString(font, Component.translatable("gui.estherserver.prof_inventory.overview"), x, y, TEXT_COLOR)
        y += 14

        for (profession in Profession.entries) {
            val level = profData.getLevel(profession)
            val slots = ProfessionBonusHelper.getInventorySlots(level)
            val invData = ProfessionInventoryClientHandler.cachedData
            val used = invData.getUsedSlotCount(profession)

            val text = Component.translatable(profession.translationKey)
                .append(Component.literal(": Lv$level"))
                .append(Component.literal(" ($used/$slots)"))

            guiGraphics.drawString(font, text, x + 4, y, TEXT_LIGHT)
            y += 14
        }
    }

    private fun renderProfessionTab(guiGraphics: GuiGraphics, startY: Int, profession: Profession) {
        val profData = ProfessionClientHandler.cachedData
        val level = profData.getLevel(profession)
        val totalSlots = ProfessionBonusHelper.getInventorySlots(level)
        val invData = ProfessionInventoryClientHandler.cachedData

        val x = guiLeft + PADDING
        var y = startY + 4

        // Header
        val headerText = Component.translatable(profession.translationKey)
            .append(Component.literal(" Lv$level - $totalSlots"))
            .append(Component.translatable("gui.estherserver.prof_inventory.slots"))
        guiGraphics.drawString(font, headerText, x, y, TEXT_COLOR)
        y += 16

        // Render slot grid
        for (slot in 0 until ProfessionInventoryData.MAX_SLOTS) {
            val row = slot / SLOTS_PER_ROW
            val col = slot % SLOTS_PER_ROW
            val slotX = x + col * SLOT_SIZE + 4
            val slotY = y + row * SLOT_SIZE

            if (slot < totalSlots) {
                // Available slot
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, SLOT_BG)
                val item = invData.getItem(profession, slot)
                if (!item.isEmpty) {
                    guiGraphics.renderItem(item, slotX + 1, slotY + 1)
                    guiGraphics.renderItemDecorations(font, item, slotX + 1, slotY + 1)
                }
            } else {
                // Locked slot (greyed out)
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, SLOT_LOCKED)
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        // Tab clicking
        val tabY = guiTop + 18
        for (i in TABS.indices) {
            val tabX = guiLeft + PADDING + i * (TAB_WIDTH + 2)
            if (mouseX >= tabX && mouseX < tabX + TAB_WIDTH &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
            ) {
                selectedTab = i
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isPauseScreen(): Boolean = false
}
