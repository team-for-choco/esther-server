package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.economy.EconomyClientHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.neoforge.network.PacketDistributor
import java.util.Optional

class ShopScreen : Screen(Component.translatable("gui.estherserver.shop.title")) {

    companion object {
        private const val GUI_WIDTH = 220
        private const val GUI_HEIGHT = 200
        private const val COLUMNS = 4
        private const val CELL_WIDTH = 46
        private const val CELL_HEIGHT = 36
        private const val PADDING = 10
        private const val TAB_HEIGHT = 16
        private const val TAB_GAP = 2

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val BG_DARK = 0xFF8B8B8B.toInt()
        private val CELL_BG = 0xFF4A4A4A.toInt()
        private val CELL_HOVER = 0xFF5A5A5A.toInt()
        private val GOLD_COLOR = 0xFFFFD700.toInt()
        private val INSUFFICIENT_COLOR = 0xFFFF6666.toInt()
    }

    private data class TabEntry(val category: ShopCategory?, val translationKey: String)

    private val tabs = listOf(
        TabEntry(null, "gui.estherserver.shop.tab.all"),
        TabEntry(ShopCategory.SEEDS, ShopCategory.SEEDS.translationKey),
        TabEntry(ShopCategory.FOOD, ShopCategory.FOOD.translationKey),
        TabEntry(ShopCategory.MINERALS, ShopCategory.MINERALS.translationKey),
        TabEntry(ShopCategory.SPECIAL, ShopCategory.SPECIAL.translationKey)
    )

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedCategory: ShopCategory? = null
    private var scrollOffset = 0
    private val itemCache = mutableMapOf<String, ItemStack>()

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        buildItemCache()
    }

    private fun buildItemCache() {
        itemCache.clear()
        for (entry in ShopBuyRegistry.getAllEntries()) {
            val item = BuiltInRegistries.ITEM.getValue(entry.itemId)
            if (item !== Items.AIR) {
                itemCache[entry.itemId.toString()] = ItemStack(item)
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderTabs(guiGraphics, mouseX, mouseY)
        renderGrid(guiGraphics, mouseX, mouseY)
        renderBalance(guiGraphics)
        renderTooltips(guiGraphics, mouseX, mouseY)
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())

        // Grid area background
        val gridY = guiTop + 38
        val gridHeight = getGridHeight()
        guiGraphics.fill(
            guiLeft + PADDING - 1, gridY - 1,
            guiLeft + GUI_WIDTH - PADDING + 1, gridY + gridHeight + 1,
            BG_DARK
        )
    }

    private fun renderTitle(guiGraphics: GuiGraphics) {
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.shop.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            0xFF404040.toInt()
        )
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        var tabX = guiLeft + PADDING
        val tabY = guiTop + 19

        for (tab in tabs) {
            val label = Component.translatable(tab.translationKey)
            val tabWidth = font.width(label) + 8
            val isSelected = selectedCategory == tab.category

            val isHovered = mouseX >= tabX && mouseX < tabX + tabWidth &&
                mouseY >= tabY && mouseY < tabY + TAB_HEIGHT

            val bgColor = when {
                isSelected -> 0xFFFFFFFF.toInt()
                isHovered -> 0xFFDDDDDD.toInt()
                else -> 0xFFAAAAAA.toInt()
            }
            guiGraphics.fill(tabX, tabY, tabX + tabWidth, tabY + TAB_HEIGHT, bgColor)
            guiGraphics.renderOutline(tabX, tabY, tabWidth, TAB_HEIGHT, 0xFF000000.toInt())
            guiGraphics.drawString(
                font, label,
                tabX + 4, tabY + 4,
                if (isSelected) 0xFF000000.toInt() else 0xFF404040.toInt()
            )
            tabX += tabWidth + TAB_GAP
        }
    }

    private fun renderGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val entries = getVisibleEntries()
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val balance = EconomyClientHandler.cachedBalance
        val gridWidth = GUI_WIDTH - PADDING * 2

        // Scissor for scrollable content
        val maxVisibleRows = getMaxVisibleRows()
        val visibleHeight = maxVisibleRows * CELL_HEIGHT
        guiGraphics.enableScissor(
            guiLeft + PADDING, startY,
            guiLeft + GUI_WIDTH - PADDING, startY + visibleHeight
        )

        for ((index, entry) in entries.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val cellX = startX + col * CELL_WIDTH
            val cellY = startY + (row - scrollOffset) * CELL_HEIGHT

            if (cellY < startY - CELL_HEIGHT || cellY > startY + visibleHeight) continue

            val isHovered = mouseX >= cellX && mouseX < cellX + CELL_WIDTH &&
                mouseY >= cellY && mouseY < cellY + CELL_HEIGHT &&
                mouseY >= startY && mouseY < startY + visibleHeight

            val bgColor = if (isHovered) CELL_HOVER else CELL_BG
            guiGraphics.fill(cellX + 1, cellY + 1, cellX + CELL_WIDTH - 1, cellY + CELL_HEIGHT - 1, bgColor)

            // Item icon
            val stack = itemCache[entry.itemId.toString()]
            if (stack != null) {
                guiGraphics.renderItem(stack, cellX + (CELL_WIDTH - 16) / 2, cellY + 2)
            }

            // Price text
            val priceText = "${entry.buyPrice} 기운"
            val priceColor = if (balance >= entry.buyPrice) GOLD_COLOR else INSUFFICIENT_COLOR
            val priceWidth = font.width(priceText)
            guiGraphics.drawString(
                font, priceText,
                cellX + (CELL_WIDTH - priceWidth) / 2, cellY + CELL_HEIGHT - 12,
                priceColor
            )
        }

        guiGraphics.disableScissor()

        // Scrollbar
        val totalRows = (entries.size + COLUMNS - 1) / COLUMNS
        if (totalRows > maxVisibleRows) {
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (maxVisibleRows.toFloat() / totalRows * visibleHeight).toInt().coerceAtLeast(8)
            val maxScroll = getMaxScroll()
            val scrollbarY = if (maxScroll > 0) {
                startY + (scrollOffset.toFloat() / maxScroll * (visibleHeight - scrollbarHeight)).toInt()
            } else startY
            guiGraphics.fill(scrollbarX, startY, scrollbarX + 3, startY + visibleHeight, 0xFF555555.toInt())
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, 0xFFAAAAAA.toInt())
        }
    }

    private fun renderBalance(guiGraphics: GuiGraphics) {
        val balance = EconomyClientHandler.cachedBalance
        val balanceText = Component.translatable("gui.estherserver.shop.balance", balance)
        guiGraphics.drawCenteredString(
            font, balanceText,
            guiLeft + GUI_WIDTH / 2,
            guiTop + GUI_HEIGHT - 16,
            GOLD_COLOR
        )
    }

    private fun renderTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val entries = getVisibleEntries()
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val maxVisibleRows = getMaxVisibleRows()
        val visibleHeight = maxVisibleRows * CELL_HEIGHT

        for ((index, entry) in entries.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val cellX = startX + col * CELL_WIDTH
            val cellY = startY + (row - scrollOffset) * CELL_HEIGHT

            if (cellY < startY || cellY + CELL_HEIGHT > startY + visibleHeight) continue

            if (mouseX >= cellX && mouseX < cellX + CELL_WIDTH &&
                mouseY >= cellY && mouseY < cellY + CELL_HEIGHT
            ) {
                val stack = itemCache[entry.itemId.toString()]
                if (stack != null) {
                    val tooltipLines = mutableListOf<Component>()
                    tooltipLines.add(stack.hoverName)
                    tooltipLines.add(
                        Component.translatable("gui.estherserver.shop.price", entry.buyPrice)
                            .withStyle { it.withColor(0xFFD700) }
                    )
                    tooltipLines.add(
                        Component.translatable("gui.estherserver.shop.click_to_buy")
                            .withStyle { it.withColor(0xAAAAAA) }
                    )
                    guiGraphics.renderTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY)
                }
                break
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            // Tab click
            var tabX = guiLeft + PADDING
            val tabY = guiTop + 19

            for (tab in tabs) {
                val label = Component.translatable(tab.translationKey)
                val tabWidth = font.width(label) + 8

                if (mouseX >= tabX && mouseX < tabX + tabWidth &&
                    mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
                ) {
                    selectedCategory = tab.category
                    scrollOffset = 0
                    return true
                }
                tabX += tabWidth + TAB_GAP
            }

            // Grid item click
            val entries = getVisibleEntries()
            val startX = guiLeft + PADDING
            val startY = guiTop + 38
            val maxVisibleRows = getMaxVisibleRows()
            val visibleHeight = maxVisibleRows * CELL_HEIGHT

            for ((index, entry) in entries.withIndex()) {
                val col = index % COLUMNS
                val row = index / COLUMNS
                val cellX = startX + col * CELL_WIDTH
                val cellY = startY + (row - scrollOffset) * CELL_HEIGHT

                if (cellY < startY || cellY + CELL_HEIGHT > startY + visibleHeight) continue

                if (mouseX >= cellX && mouseX < cellX + CELL_WIDTH &&
                    mouseY >= cellY && mouseY < cellY + CELL_HEIGHT
                ) {
                    val quantity = if (hasShiftDown()) 16 else 1
                    PacketDistributor.sendToServer(BuyItemPayload(entry.itemId.toString(), quantity))
                    return true
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val maxScroll = getMaxScroll()
        if (maxScroll > 0) {
            scrollOffset = (scrollOffset - scrollY.toInt()).coerceIn(0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun isPauseScreen(): Boolean = false

    private fun getVisibleEntries(): List<ShopEntry> {
        val allEntries = ShopBuyRegistry.getAllEntries()
        return selectedCategory?.let { category -> allEntries.filter { it.category == category } } ?: allEntries
    }

    private fun getMaxVisibleRows(): Int = 4

    private fun getGridHeight(): Int = getMaxVisibleRows() * CELL_HEIGHT

    private fun getMaxScroll(): Int {
        val entries = getVisibleEntries()
        val totalRows = (entries.size + COLUMNS - 1) / COLUMNS
        return (totalRows - getMaxVisibleRows()).coerceAtLeast(0)
    }
}
