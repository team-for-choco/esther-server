package com.juyoung.estherserver.cosmetic

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.sitting.ModKeyBindings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.network.PacketDistributor

@OnlyIn(Dist.CLIENT)
class CosmeticScreen : Screen(Component.translatable("gui.estherserver.cosmetic.title")) {

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedTab = 0 // 0=HEAD, 1=CHEST, 2=LEGS, 3=FEET
    private var scrollOffset = 0
    private val itemStackCache = mutableMapOf<String, ItemStack>()

    companion object {
        private const val GUI_WIDTH = 300
        private const val GUI_HEIGHT = 210

        // Left panel — player preview
        private const val PREVIEW_LEFT = 10
        private const val PREVIEW_TOP = 24
        private const val PREVIEW_WIDTH = 80
        private const val PREVIEW_HEIGHT = 150

        // Right panel — cosmetics grid
        private const val GRID_LEFT = 100
        private const val GRID_TOP = 44
        private const val CELL_SIZE = 36
        private const val CELL_GAP = 4
        private const val CELLS_PER_ROW = 4
        private const val MAX_VISIBLE_ROWS = 4

        // Tabs
        private const val TAB_TOP = 24
        private const val TAB_WIDTH = 46
        private const val TAB_HEIGHT = 16
        private const val TAB_GAP = 2

        // Equip info
        private const val INFO_Y = 188

        private val SLOT_ORDER = listOf(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        )
        private val TAB_KEYS = listOf(
            "gui.estherserver.cosmetic.tab.head",
            "gui.estherserver.cosmetic.tab.chest",
            "gui.estherserver.cosmetic.tab.legs",
            "gui.estherserver.cosmetic.tab.feet"
        )
    }

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        scrollOffset = 0
    }

    private fun currentSlot(): EquipmentSlot = SLOT_ORDER[selectedTab]

    private fun getVisibleCosmetics(): List<CosmeticDef> {
        val slot = currentSlot()
        val unlocked = CosmeticClientHandler.myUnlockedCosmetics
        return CosmeticRegistry.getBySlot(slot).filter { it.id in unlocked }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Main panel
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // Title
        guiGraphics.drawCenteredString(font, title, guiLeft + GUI_WIDTH / 2, guiTop + 8, GuiTheme.TEXT_TITLE)

        // Left panel: player preview
        renderPlayerPreview(guiGraphics, mouseX, mouseY)

        // Divider
        val divX = guiLeft + PREVIEW_LEFT + PREVIEW_WIDTH + 4
        guiGraphics.fill(divX, guiTop + PREVIEW_TOP, divX + 1, guiTop + GUI_HEIGHT - 14, GuiTheme.PANEL_BORDER_DARK)

        // Right panel: tabs + grid
        renderTabs(guiGraphics, mouseX, mouseY)
        renderCosmeticGrid(guiGraphics, mouseX, mouseY)

        // Bottom info bar
        renderInfoBar(guiGraphics)
    }

    private fun renderPlayerPreview(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val px = guiLeft + PREVIEW_LEFT
        val py = guiTop + PREVIEW_TOP
        GuiTheme.renderInnerPanel(guiGraphics, px, py, PREVIEW_WIDTH, PREVIEW_HEIGHT)

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                px + 4, py + 4,
                px + PREVIEW_WIDTH - 4, py + PREVIEW_HEIGHT - 4,
                35,
                0.0625f,
                mouseX.toFloat(), mouseY.toFloat(),
                player
            )
        } catch (_: Exception) {
            guiGraphics.drawCenteredString(
                font, "?",
                px + PREVIEW_WIDTH / 2, py + PREVIEW_HEIGHT / 2,
                GuiTheme.TEXT_MUTED
            )
        }
    }

    private fun renderTabs(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        for (i in TAB_KEYS.indices) {
            val tabX = guiLeft + GRID_LEFT + i * (TAB_WIDTH + TAB_GAP)
            val tabY = guiTop + TAB_TOP
            val isActive = i == selectedTab
            val isHovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH &&
                    mouseY >= tabY && mouseY < tabY + TAB_HEIGHT

            val bg = when {
                isActive -> GuiTheme.TAB_ACTIVE
                isHovered -> GuiTheme.TAB_HOVER
                else -> GuiTheme.TAB_INACTIVE
            }
            guiGraphics.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, bg)

            // Tab border
            if (isActive) {
                guiGraphics.fill(tabX, tabY + TAB_HEIGHT - 1, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, GuiTheme.PANEL_BORDER_LIGHT)
            }

            val label = Component.translatable(TAB_KEYS[i])
            guiGraphics.drawCenteredString(
                font, label,
                tabX + TAB_WIDTH / 2, tabY + 4,
                if (isActive) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_MUTED
            )
        }
    }

    private fun getTokenStack(def: CosmeticDef): ItemStack {
        return itemStackCache.getOrPut(def.id) {
            val item = BuiltInRegistries.ITEM.getValue(
                ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, def.tokenItemId)
            )
            ItemStack(item)
        }
    }

    private fun renderCosmeticGrid(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val cosmetics = getVisibleCosmetics()
        val equippedId = CosmeticClientHandler.myEquipped[currentSlot()]

        val gridX = guiLeft + GRID_LEFT
        val gridY = guiTop + GRID_TOP

        // Grid background
        val gridW = CELLS_PER_ROW * (CELL_SIZE + CELL_GAP) - CELL_GAP
        val gridH = MAX_VISIBLE_ROWS * (CELL_SIZE + CELL_GAP) - CELL_GAP
        GuiTheme.renderInnerPanel(guiGraphics, gridX - 2, gridY - 2, gridW + 4, gridH + 4)

        // "해제" 버튼 (첫 번째 셀)
        val unequipX = gridX
        val unequipY = gridY
        val isUnequipHovered = mouseX >= unequipX && mouseX < unequipX + CELL_SIZE &&
                mouseY >= unequipY && mouseY < unequipY + CELL_SIZE
        val isNoneEquipped = equippedId == null

        val unequipBg = when {
            isNoneEquipped -> GuiTheme.ROW_SELECTED
            isUnequipHovered -> GuiTheme.CELL_HOVER
            else -> GuiTheme.CELL_BG
        }
        guiGraphics.fill(unequipX, unequipY, unequipX + CELL_SIZE, unequipY + CELL_SIZE, unequipBg)

        // Border for unequip cell
        val unequipBorder = if (isNoneEquipped) GuiTheme.PANEL_BORDER_LIGHT else GuiTheme.PANEL_BORDER_DARK
        guiGraphics.fill(unequipX, unequipY, unequipX + CELL_SIZE, unequipY + 1, unequipBorder)
        guiGraphics.fill(unequipX, unequipY + CELL_SIZE - 1, unequipX + CELL_SIZE, unequipY + CELL_SIZE, unequipBorder)
        guiGraphics.fill(unequipX, unequipY, unequipX + 1, unequipY + CELL_SIZE, unequipBorder)
        guiGraphics.fill(unequipX + CELL_SIZE - 1, unequipY, unequipX + CELL_SIZE, unequipY + CELL_SIZE, unequipBorder)

        val removeLabel = Component.translatable("gui.estherserver.cosmetic.remove")
        guiGraphics.drawCenteredString(font, removeLabel, unequipX + CELL_SIZE / 2, unequipY + CELL_SIZE / 2 - 4, GuiTheme.TEXT_MUTED)

        // Hover tooltip for unequip cell
        if (isUnequipHovered) {
            guiGraphics.renderTooltip(font, Component.translatable("gui.estherserver.cosmetic.remove"), mouseX, mouseY)
        }

        // 호버된 셀의 툴팁을 아이템 렌더링 후에 그리기 위해 저장
        var hoveredTooltip: (() -> Unit)? = null

        // Render cosmetic cells (획득한 것만 표시)
        for (i in cosmetics.indices) {
            val cellIndex = i + 1 // offset by 1 for unequip button
            val col = cellIndex % CELLS_PER_ROW
            val row = cellIndex / CELLS_PER_ROW
            if (row >= MAX_VISIBLE_ROWS) break

            val cellX = gridX + col * (CELL_SIZE + CELL_GAP)
            val cellY = gridY + row * (CELL_SIZE + CELL_GAP)

            val def = cosmetics[i]
            val isEquipped = def.id == equippedId
            val isHovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE &&
                    mouseY >= cellY && mouseY < cellY + CELL_SIZE

            val bg = when {
                isEquipped -> GuiTheme.ROW_SELECTED
                isHovered -> GuiTheme.CELL_HOVER
                else -> GuiTheme.CELL_BG
            }
            guiGraphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, bg)

            // Grade border
            val bw = if (isEquipped) 2 else 1
            guiGraphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + bw, def.grade.color)
            guiGraphics.fill(cellX, cellY + CELL_SIZE - bw, cellX + CELL_SIZE, cellY + CELL_SIZE, def.grade.color)
            guiGraphics.fill(cellX, cellY, cellX + bw, cellY + CELL_SIZE, def.grade.color)
            guiGraphics.fill(cellX + CELL_SIZE - bw, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, def.grade.color)

            // 아이템 아이콘 렌더링 (16x16, 셀 중앙)
            val stack = getTokenStack(def)
            val iconX = cellX + (CELL_SIZE - 16) / 2
            val iconY = cellY + (CELL_SIZE - 16) / 2
            guiGraphics.renderItem(stack, iconX, iconY)

            // Equipped indicator (좌상단 금색 마크)
            if (isEquipped) {
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.cosmetic.equipped_mark"),
                    cellX + 2, cellY + 2, GuiTheme.TEXT_GOLD
                )
            }

            // Tooltip on hover
            if (isHovered) {
                hoveredTooltip = {
                    val tooltipLines = mutableListOf<Component>()
                    tooltipLines.add(Component.translatable(def.displayKey).withStyle { it.withColor(def.grade.color) })
                    tooltipLines.add(Component.translatable("cosmetic.estherserver.grade_label")
                        .append(": ")
                        .append(Component.translatable(def.grade.translationKey)))
                    if (isEquipped) {
                        tooltipLines.add(Component.translatable("gui.estherserver.cosmetic.click_to_remove"))
                    } else {
                        tooltipLines.add(Component.translatable("gui.estherserver.cosmetic.click_to_equip"))
                    }
                    guiGraphics.renderTooltip(font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY)
                }
            }
        }

        // 툴팁은 모든 셀 렌더링 후 마지막에 그림
        hoveredTooltip?.invoke()
    }

    private fun renderInfoBar(guiGraphics: GuiGraphics) {
        val equippedId = CosmeticClientHandler.myEquipped[currentSlot()]
        val infoY = guiTop + INFO_Y

        if (equippedId != null) {
            val def = CosmeticRegistry.get(equippedId)
            if (def != null) {
                val label = Component.translatable("gui.estherserver.cosmetic.wearing")
                    .append(": ")
                    .append(Component.translatable(def.displayKey))
                guiGraphics.drawCenteredString(font, label, guiLeft + GUI_WIDTH / 2, infoY, def.grade.color)
                return
            }
        }
        val label = Component.translatable("gui.estherserver.cosmetic.none_equipped")
        guiGraphics.drawCenteredString(font, label, guiLeft + GUI_WIDTH / 2, infoY, GuiTheme.TEXT_MUTED)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        // Tab clicks
        for (i in TAB_KEYS.indices) {
            val tabX = guiLeft + GRID_LEFT + i * (TAB_WIDTH + TAB_GAP)
            val tabY = guiTop + TAB_TOP
            if (mx >= tabX && mx < tabX + TAB_WIDTH && my >= tabY && my < tabY + TAB_HEIGHT) {
                selectedTab = i
                scrollOffset = 0
                return true
            }
        }

        val gridX = guiLeft + GRID_LEFT
        val gridY = guiTop + GRID_TOP

        // Unequip button click (cell 0)
        if (mx >= gridX && mx < gridX + CELL_SIZE && my >= gridY && my < gridY + CELL_SIZE) {
            PacketDistributor.sendToServer(EquipCosmeticPayload(currentSlot().name, ""))
            return true
        }

        // Cosmetic cell clicks (획득한 것만 표시되므로 모두 클릭 가능)
        val cosmetics = getVisibleCosmetics()

        for (i in cosmetics.indices) {
            val cellIndex = i + 1
            val col = cellIndex % CELLS_PER_ROW
            val row = cellIndex / CELLS_PER_ROW
            if (row >= MAX_VISIBLE_ROWS) break

            val cellX = gridX + col * (CELL_SIZE + CELL_GAP)
            val cellY = gridY + row * (CELL_SIZE + CELL_GAP)

            if (mx >= cellX && mx < cellX + CELL_SIZE && my >= cellY && my < cellY + CELL_SIZE) {
                val def = cosmetics[i]
                val equippedId = CosmeticClientHandler.myEquipped[currentSlot()]
                if (def.id == equippedId) {
                    PacketDistributor.sendToServer(EquipCosmeticPayload(currentSlot().name, ""))
                } else {
                    PacketDistributor.sendToServer(EquipCosmeticPayload(currentSlot().name, def.id))
                }
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ModKeyBindings.COSMETIC_KEY.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean = false
}
