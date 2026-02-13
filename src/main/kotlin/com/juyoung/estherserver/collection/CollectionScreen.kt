package com.juyoung.estherserver.collection

import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val BG_DARK = 0xFF8B8B8B.toInt()
        private val SLOT_BG = 0xFF8B8B8B.toInt()
        private val SLOT_INNER = 0xFF373737.toInt()
        private val UNDISCOVERED_BG = 0xFF555555.toInt()
    }

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedCategory: CollectionCategory? = null
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
                val stack = ItemStack(item)
                if (def.key.quality != null) {
                    stack.set(ModDataComponents.ITEM_QUALITY.get(), def.key.quality)
                }
                itemCache[def.key] = stack
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderTabs(guiGraphics, mouseX, mouseY)
        renderGrid(guiGraphics, mouseX, mouseY)
        renderProgress(guiGraphics)
        renderTooltips(guiGraphics, mouseX, mouseY)
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        // Main background
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        // Border
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())
        // Inner dark area for grid
        val gridY = guiTop + 38
        val gridHeight = getVisibleDefinitions().size.let { count ->
            val rows = (count + COLUMNS - 1) / COLUMNS
            rows.coerceAtLeast(1) * SLOT_SIZE + 4
        }
        guiGraphics.fill(
            guiLeft + PADDING - 1, gridY - 1,
            guiLeft + PADDING + COLUMNS * SLOT_SIZE + 1, gridY + gridHeight + 1,
            BG_DARK
        )
    }

    private fun renderTitle(guiGraphics: GuiGraphics) {
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.collection.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            0xFF404040.toInt()
        )
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val tabs = listOf<Pair<CollectionCategory?, String>>(
            null to "gui.estherserver.collection.tab.all",
            CollectionCategory.FISH to CollectionCategory.FISH.translationKey,
            CollectionCategory.CROPS to CollectionCategory.CROPS.translationKey,
            CollectionCategory.MINERALS to CollectionCategory.MINERALS.translationKey,
            CollectionCategory.COOKING to CollectionCategory.COOKING.translationKey
        )

        var tabX = guiLeft + PADDING
        val tabY = guiTop + 19

        for ((category, translationKey) in tabs) {
            val label = Component.translatable(translationKey)
            val tabWidth = font.width(label) + 8
            val isSelected = selectedCategory == category
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
                // Quality-colored border
                val qualityColor = getQualityColor(def.key.quality)
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, qualityColor)
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, SLOT_INNER)

                // Render item icon
                val stack = itemCache[def.key]
                if (stack != null) {
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1)
                }
            } else {
                // Undiscovered slot
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF666666.toInt())
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, UNDISCOVERED_BG)
                guiGraphics.drawCenteredString(font, "?", slotX + 9, slotY + 5, 0xFF888888.toInt())
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

        // Progress bar background
        val barX = guiLeft + PADDING
        val barWidth = COLUMNS * SLOT_SIZE
        guiGraphics.fill(barX, progressY, barX + barWidth, progressY + 10, BG_DARK)

        // Progress bar fill
        val fillWidth = if (total > 0) barWidth * completed / total else 0
        guiGraphics.fill(barX, progressY, barX + fillWidth, progressY + 10, 0xFF55AA55.toInt())

        // Progress text
        val progressText = Component.translatable(
            "gui.estherserver.collection.progress", completed, total, percentage
        )
        guiGraphics.drawCenteredString(font, progressText, guiLeft + GUI_WIDTH / 2, progressY + 14, 0xFFFFFFFF.toInt())
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
                    val tooltipLines = mutableListOf<Component>()
                    val stack = itemCache[def.key]
                    if (stack != null) {
                        tooltipLines.add(stack.hoverName)
                    }
                    if (def.key.quality != null) {
                        tooltipLines.add(
                            Component.translatable(def.key.quality.translationKey)
                                .withStyle(def.key.quality.color)
                        )
                    }
                    guiGraphics.renderTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY)
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
            val tabs = listOf<CollectionCategory?>(
                null,
                CollectionCategory.FISH,
                CollectionCategory.CROPS,
                CollectionCategory.MINERALS,
                CollectionCategory.COOKING
            )
            val tabTranslationKeys = listOf(
                "gui.estherserver.collection.tab.all",
                CollectionCategory.FISH.translationKey,
                CollectionCategory.CROPS.translationKey,
                CollectionCategory.MINERALS.translationKey,
                CollectionCategory.COOKING.translationKey
            )

            var tabX = guiLeft + PADDING
            val tabY = guiTop + 19

            for (i in tabs.indices) {
                val label = Component.translatable(tabTranslationKeys[i])
                val tabWidth = font.width(label) + 8

                if (mouseX >= tabX && mouseX < tabX + tabWidth &&
                    mouseY >= tabY && mouseY < tabY + TAB_HEIGHT
                ) {
                    selectedCategory = tabs[i]
                    return true
                }
                tabX += tabWidth + TAB_GAP
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isPauseScreen(): Boolean = false

    private fun getVisibleDefinitions(): List<CollectibleDefinition> {
        return if (selectedCategory == null) {
            CollectibleRegistry.getAllDefinitions()
        } else {
            CollectibleRegistry.getDefinitionsByCategory(selectedCategory!!)
        }
    }

    private fun getQualityColor(quality: ItemQuality?): Int {
        if (quality == null) return 0xFFAAAAAA.toInt()
        val colorValue = quality.color.getColor() ?: return 0xFFAAAAAA.toInt()
        return (0xFF shl 24) or colorValue
    }
}
