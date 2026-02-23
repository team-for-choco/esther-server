package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionClientHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.network.PacketDistributor

class ProfessionInventoryContainerScreen(
    menu: ProfessionInventoryMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<ProfessionInventoryMenu>(menu, playerInventory, title) {

    companion object {
        private const val TAB_WIDTH = 50
        private const val TAB_HEIGHT = 18
        private const val PADDING = 8

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val TAB_ACTIVE = 0xFFAAAAAA.toInt()
        private val TAB_INACTIVE = 0xFF888888.toInt()
        private val SLOT_BG = 0xFF8B8B8B.toInt()
        private val SLOT_LOCKED = 0xFF555555.toInt()
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
            null,
            Profession.MINING,
            Profession.FISHING,
            Profession.FARMING,
            Profession.COOKING
        )
    }

    init {
        imageWidth = 280
        imageHeight = 222
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
        titleLabelY = 4
        inventoryLabelX = ProfessionInventoryMenu.PLAYER_INV_X
        inventoryLabelY = ProfessionInventoryMenu.PLAYER_INV_Y - 11
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // Background panel
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BG_COLOR)
        guiGraphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF000000.toInt())

        // Tabs
        renderTabs(guiGraphics)

        // Profession slot backgrounds
        if (menu.currentTab == 0) {
            renderGeneralOverlay(guiGraphics)
        } else {
            renderProfessionSlotBackgrounds(guiGraphics)
        }

        // Player inventory slot backgrounds
        for (i in ProfessionInventoryMenu.PROFESSION_SLOT_COUNT until menu.slots.size) {
            val slot = menu.slots[i]
            guiGraphics.fill(
                leftPos + slot.x - 1, topPos + slot.y - 1,
                leftPos + slot.x + 17, topPos + slot.y + 17,
                SLOT_BG
            )
        }
    }

    private fun renderTabs(guiGraphics: GuiGraphics) {
        val tabY = topPos + 18
        for (i in TABS.indices) {
            val tabX = leftPos + PADDING + i * (TAB_WIDTH + 2)
            val isActive = i == menu.currentTab
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

    private fun renderGeneralOverlay(guiGraphics: GuiGraphics) {
        // Dark overlay for all profession slots
        for (i in 0 until ProfessionInventoryMenu.PROFESSION_SLOT_COUNT) {
            val slot = menu.slots[i]
            guiGraphics.fill(
                leftPos + slot.x - 1, topPos + slot.y - 1,
                leftPos + slot.x + 17, topPos + slot.y + 17,
                SLOT_LOCKED
            )
        }

        // Overview text
        val profData = ProfessionClientHandler.cachedData
        val invData = ProfessionInventoryClientHandler.cachedData
        val x = leftPos + PADDING
        var y = topPos + ProfessionInventoryMenu.PROFESSION_SLOT_Y

        guiGraphics.drawString(
            font,
            Component.translatable("gui.estherserver.prof_inventory.overview"),
            x, y, TEXT_COLOR
        )
        y += 14

        for (profession in Profession.entries) {
            val level = profData.getLevel(profession)
            val slots = ProfessionBonusHelper.getInventorySlots(level)
            val used = invData.getUsedSlotCount(profession)

            val text = Component.translatable(profession.translationKey)
                .append(Component.literal(": Lv$level"))
                .append(Component.literal(" ($used/$slots)"))

            guiGraphics.drawString(font, text, x + 4, y, TEXT_LIGHT)
            y += 14
        }
    }

    private fun renderProfessionSlotBackgrounds(guiGraphics: GuiGraphics) {
        // Profession header
        val profession = TAB_PROFESSIONS[menu.currentTab]
        if (profession != null) {
            val profData = ProfessionClientHandler.cachedData
            val level = profData.getLevel(profession)
            val headerText = Component.translatable(profession.translationKey)
                .append(Component.literal(" Lv$level - ${menu.unlockedSlots}"))
                .append(Component.translatable("gui.estherserver.prof_inventory.slots"))
            guiGraphics.drawString(
                font, headerText,
                leftPos + PADDING, topPos + ProfessionInventoryMenu.PROFESSION_SLOT_Y - 10,
                TEXT_COLOR
            )
        }

        // Slot backgrounds
        for (i in 0 until ProfessionInventoryMenu.PROFESSION_SLOT_COUNT) {
            val slot = menu.slots[i]
            val color = if (i < menu.unlockedSlots) SLOT_BG else SLOT_LOCKED
            guiGraphics.fill(
                leftPos + slot.x - 1, topPos + slot.y - 1,
                leftPos + slot.x + 17, topPos + slot.y + 17,
                color
            )
        }
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, TEXT_COLOR, false)
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_COLOR, false)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val tabY = topPos + 18
        for (i in TABS.indices) {
            val tabX = leftPos + PADDING + i * (TAB_WIDTH + 2)
            if (mouseX >= tabX && mouseX < tabX + TAB_WIDTH &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
            ) {
                if (i != menu.currentTab) {
                    PacketDistributor.sendToServer(ProfessionInventoryPayload.TabSwitchPayload(i))
                }
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    fun handleTabSync(tab: Int, unlocked: Int) {
        menu.currentTab = tab
        menu.unlockedSlots = unlocked
    }
}
