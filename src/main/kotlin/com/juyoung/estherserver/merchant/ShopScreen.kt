package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.economy.ItemPriceRegistry
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.neoforge.network.PacketDistributor
import java.util.Optional

class ShopScreen(private val merchantType: ShopCategory, private val entityId: Int) :
    Screen(Component.translatable("gui.estherserver.shop.title.${merchantType.name.lowercase()}")) {

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

    private enum class ShopMode(val translationKey: String) {
        BUY("gui.estherserver.shop.tab.buy"),
        SELL("gui.estherserver.shop.tab.sell")
    }

    data class SellableSlot(val slotIndex: Int, val stack: ItemStack, val pricePerItem: Long, val quality: ItemQuality?)

    private var guiLeft = 0
    private var guiTop = 0
    private var currentMode = ShopMode.BUY
    private var scrollOffset = 0
    private val buyItemCache = mutableMapOf<String, ItemStack>()
    private var sellableSlots = listOf<SellableSlot>()

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        buildBuyItemCache()
        scanSellableItems()
    }

    private fun buildBuyItemCache() {
        buyItemCache.clear()
        for (entry in ShopBuyRegistry.getAllEntries()) {
            if (entry.category != merchantType) continue
            val item = BuiltInRegistries.ITEM.getValue(entry.itemId)
            if (item !== Items.AIR) {
                buyItemCache[entry.itemId.toString()] = ItemStack(item)
            }
        }
    }

    private fun scanSellableItems() {
        val player = Minecraft.getInstance().player ?: return
        val result = mutableListOf<SellableSlot>()
        for (i in 0..35) {
            val stack = player.inventory.getItem(i)
            if (stack.isEmpty) continue
            val itemId = stack.itemHolder.unwrapKey().orElse(null)?.location() ?: continue
            val category = ItemPriceRegistry.getCategory(itemId) ?: continue
            if (category != merchantType) continue
            val price = ItemPriceRegistry.getPrice(stack) ?: continue
            val quality = stack.get(ModDataComponents.ITEM_QUALITY.get())
            result.add(SellableSlot(i, stack.copy(), price, quality))
        }
        sellableSlots = result
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderModeTabs(guiGraphics, mouseX, mouseY)
        when (currentMode) {
            ShopMode.BUY -> renderBuyGrid(guiGraphics, mouseX, mouseY)
            ShopMode.SELL -> renderSellGrid(guiGraphics, mouseX, mouseY)
        }
        renderBalance(guiGraphics)
        when (currentMode) {
            ShopMode.BUY -> renderBuyTooltips(guiGraphics, mouseX, mouseY)
            ShopMode.SELL -> renderSellTooltips(guiGraphics, mouseX, mouseY)
        }
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())

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
            Component.translatable("gui.estherserver.shop.title.${merchantType.name.lowercase()}"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            0xFF404040.toInt()
        )
    }

    private fun renderModeTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        var tabX = guiLeft + PADDING
        val tabY = guiTop + 19

        for (mode in ShopMode.entries) {
            val label = Component.translatable(mode.translationKey)
            val tabWidth = font.width(label) + 8
            val isSelected = currentMode == mode

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

    private fun renderBuyGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val entries = getBuyEntries()
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val balance = EconomyClientHandler.cachedBalance

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

            val stack = buyItemCache[entry.itemId.toString()]
            if (stack != null) {
                guiGraphics.renderItem(stack, cellX + (CELL_WIDTH - 16) / 2, cellY + 2)
            }

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
        renderScrollbar(guiGraphics, entries.size, startY, visibleHeight)
    }

    private fun renderSellGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val maxVisibleRows = getMaxVisibleRows()
        val visibleHeight = maxVisibleRows * CELL_HEIGHT

        guiGraphics.enableScissor(
            guiLeft + PADDING, startY,
            guiLeft + GUI_WIDTH - PADDING, startY + visibleHeight
        )

        for ((index, slot) in sellableSlots.withIndex()) {
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

            // Quality border
            val borderColor = when (slot.quality) {
                ItemQuality.FINE -> 0xFF55FF55.toInt()   // Green
                ItemQuality.RARE -> 0xFF5555FF.toInt()   // Blue
                else -> null
            }
            if (borderColor != null) {
                guiGraphics.renderOutline(cellX + 1, cellY + 1, CELL_WIDTH - 2, CELL_HEIGHT - 2, borderColor)
            }

            guiGraphics.renderItem(slot.stack, cellX + (CELL_WIDTH - 16) / 2, cellY + 2)

            // Show count overlay
            if (slot.stack.count > 1) {
                val countStr = slot.stack.count.toString()
                guiGraphics.drawString(
                    font, countStr,
                    cellX + CELL_WIDTH - 4 - font.width(countStr), cellY + 2,
                    0xFFFFFFFF.toInt()
                )
            }

            val priceText = "${slot.pricePerItem} 기운"
            val priceWidth = font.width(priceText)
            guiGraphics.drawString(
                font, priceText,
                cellX + (CELL_WIDTH - priceWidth) / 2, cellY + CELL_HEIGHT - 12,
                GOLD_COLOR
            )
        }

        guiGraphics.disableScissor()
        renderScrollbar(guiGraphics, sellableSlots.size, startY, visibleHeight)
    }

    private fun renderScrollbar(guiGraphics: GuiGraphics, totalItems: Int, startY: Int, visibleHeight: Int) {
        val maxVisibleRows = getMaxVisibleRows()
        val totalRows = (totalItems + COLUMNS - 1) / COLUMNS
        if (totalRows > maxVisibleRows) {
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (maxVisibleRows.toFloat() / totalRows * visibleHeight).toInt().coerceAtLeast(8)
            val maxScroll = (totalRows - maxVisibleRows).coerceAtLeast(0)
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

    private fun renderBuyTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val entries = getBuyEntries()
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
                val stack = buyItemCache[entry.itemId.toString()]
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

    private fun renderSellTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val maxVisibleRows = getMaxVisibleRows()
        val visibleHeight = maxVisibleRows * CELL_HEIGHT

        for ((index, slot) in sellableSlots.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val cellX = startX + col * CELL_WIDTH
            val cellY = startY + (row - scrollOffset) * CELL_HEIGHT

            if (cellY < startY || cellY + CELL_HEIGHT > startY + visibleHeight) continue

            if (mouseX >= cellX && mouseX < cellX + CELL_WIDTH &&
                mouseY >= cellY && mouseY < cellY + CELL_HEIGHT
            ) {
                val tooltipLines = mutableListOf<Component>()
                tooltipLines.add(slot.stack.hoverName)
                tooltipLines.add(
                    Component.translatable("gui.estherserver.shop.price", slot.pricePerItem)
                        .withStyle { it.withColor(0xFFD700) }
                )
                tooltipLines.add(
                    Component.literal("x${slot.stack.count}")
                        .withStyle { it.withColor(0xAAAAAA) }
                )
                tooltipLines.add(
                    Component.translatable("gui.estherserver.shop.click_to_sell")
                        .withStyle { it.withColor(0xAAAAAA) }
                )
                guiGraphics.renderTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY)
                break
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            // Mode tab click
            var tabX = guiLeft + PADDING
            val tabY = guiTop + 19

            for (mode in ShopMode.entries) {
                val label = Component.translatable(mode.translationKey)
                val tabWidth = font.width(label) + 8

                if (mouseX >= tabX && mouseX < tabX + tabWidth &&
                    mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
                ) {
                    if (currentMode != mode) {
                        currentMode = mode
                        scrollOffset = 0
                        if (mode == ShopMode.SELL) {
                            scanSellableItems()
                        }
                    }
                    return true
                }
                tabX += tabWidth + TAB_GAP
            }

            // Grid item click
            when (currentMode) {
                ShopMode.BUY -> return handleBuyClick(mouseX, mouseY)
                ShopMode.SELL -> return handleSellClick(mouseX, mouseY)
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun handleBuyClick(mouseX: Double, mouseY: Double): Boolean {
        val entries = getBuyEntries()
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
        return false
    }

    private fun handleSellClick(mouseX: Double, mouseY: Double): Boolean {
        val startX = guiLeft + PADDING
        val startY = guiTop + 38
        val maxVisibleRows = getMaxVisibleRows()
        val visibleHeight = maxVisibleRows * CELL_HEIGHT

        for ((index, slot) in sellableSlots.withIndex()) {
            val col = index % COLUMNS
            val row = index / COLUMNS
            val cellX = startX + col * CELL_WIDTH
            val cellY = startY + (row - scrollOffset) * CELL_HEIGHT

            if (cellY < startY || cellY + CELL_HEIGHT > startY + visibleHeight) continue

            if (mouseX >= cellX && mouseX < cellX + CELL_WIDTH &&
                mouseY >= cellY && mouseY < cellY + CELL_HEIGHT
            ) {
                val quantity = if (hasShiftDown()) slot.stack.count else 1
                PacketDistributor.sendToServer(SellItemPayload(entityId, slot.slotIndex, quantity))

                // Optimistic UI update
                val newCount = slot.stack.count - quantity
                if (newCount <= 0) {
                    sellableSlots = sellableSlots.toMutableList().also { it.removeAt(index) }
                } else {
                    val updatedStack = slot.stack.copy().also { it.count = newCount }
                    sellableSlots = sellableSlots.toMutableList().also {
                        it[index] = slot.copy(stack = updatedStack)
                    }
                }
                return true
            }
        }
        return false
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

    private fun getBuyEntries(): List<ShopEntry> {
        return ShopBuyRegistry.getAllEntries().filter { it.category == merchantType }
    }

    private fun getMaxVisibleRows(): Int = 4

    private fun getGridHeight(): Int = getMaxVisibleRows() * CELL_HEIGHT

    private fun getMaxScroll(): Int {
        val totalItems = when (currentMode) {
            ShopMode.BUY -> getBuyEntries().size
            ShopMode.SELL -> sellableSlots.size
        }
        val totalRows = (totalItems + COLUMNS - 1) / COLUMNS
        return (totalRows - getMaxVisibleRows()).coerceAtLeast(0)
    }
}
