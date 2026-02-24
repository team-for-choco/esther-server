package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor
import java.util.Optional

class EnhancementScreen : Screen(Component.translatable("gui.estherserver.enhancement.title")) {

    companion object {
        private const val GUI_WIDTH = 240
        private const val GUI_HEIGHT = 250
        private const val ROW_HEIGHT = 30
        private const val PADDING = 10
    }

    data class EquipmentSlot(
        val profession: Profession,
        val item: Item,
        val stack: ItemStack?,
        val level: Int // -1 if not owned
    )

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedIndex = 0
    private var equipmentSlots = listOf<EquipmentSlot>()
    private var rescanTicks = -1

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        scanEquipment()
    }

    private fun scanEquipment() {
        val player = Minecraft.getInstance().player ?: return
        val result = mutableListOf<EquipmentSlot>()

        val equipmentMap = mapOf(
            Profession.FISHING to EstherServerMod.SPECIAL_FISHING_ROD.get(),
            Profession.FARMING to EstherServerMod.SPECIAL_HOE.get(),
            Profession.MINING to EstherServerMod.SPECIAL_PICKAXE.get(),
            Profession.COOKING to EstherServerMod.SPECIAL_COOKING_TOOL.get()
        )

        for ((profession, item) in equipmentMap) {
            val found = player.inventory.items.firstOrNull { !it.isEmpty && it.item === item }

            val level = found?.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0) ?: -1
            result.add(EquipmentSlot(profession, item, found, level))
        }

        equipmentSlots = result
    }

    override fun tick() {
        super.tick()
        if (rescanTicks > 0) {
            rescanTicks--
            if (rescanTicks == 0) {
                scanEquipment()
                rescanTicks = -1
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderPanel(guiGraphics)
        renderTitle(guiGraphics)
        renderEquipmentList(guiGraphics, mouseX, mouseY)
        renderDetailPanel(guiGraphics, mouseX, mouseY)
        renderBalance(guiGraphics)
        renderTooltips(guiGraphics, mouseX, mouseY)
    }

    private fun renderPanel(guiGraphics: GuiGraphics) {
        // 텍스처 배경 (240x250 영역, 256x256 캔버스)
        guiGraphics.blit(RenderType::guiTextured, GuiTheme.ENHANCEMENT_BG, guiLeft, guiTop, 0f, 0f, GUI_WIDTH, GUI_HEIGHT, 256, 256)
    }

    private fun renderTitle(guiGraphics: GuiGraphics) {
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enhancement.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )
    }

    private fun renderEquipmentList(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val startX = guiLeft + PADDING
        val startY = guiTop + 20
        val rowWidth = GUI_WIDTH - PADDING * 2

        for ((index, slot) in equipmentSlots.withIndex()) {
            val rowY = startY + index * ROW_HEIGHT
            val isHovered = mouseX >= startX && mouseX < startX + rowWidth &&
                mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2
            val isSelected = selectedIndex == index

            val bgColor = when {
                isSelected -> GuiTheme.ROW_SELECTED
                isHovered -> GuiTheme.ROW_HOVER
                else -> GuiTheme.ROW_BG
            }

            guiGraphics.fill(startX, rowY, startX + rowWidth, rowY + ROW_HEIGHT - 2, bgColor)

            // Grade color border for owned equipment
            if (slot.level >= 0) {
                val borderColor = getGradeColor(slot.level)
                guiGraphics.renderOutline(startX, rowY, rowWidth, ROW_HEIGHT - 2, borderColor)
            }

            // Item icon
            val iconStack = slot.stack ?: ItemStack(slot.item)
            guiGraphics.renderItem(iconStack, startX + 4, rowY + (ROW_HEIGHT - 2 - 16) / 2)

            // Equipment name
            val nameColor = if (slot.level >= 0) getGradeColor(slot.level) else GuiTheme.TEXT_MUTED
            guiGraphics.drawString(
                font,
                Component.translatable(slot.item.descriptionId),
                startX + 24, rowY + 4,
                nameColor
            )

            // Level or "미보유"
            if (slot.level >= 0) {
                val gradeKey = EnhancementHandler.getGradeTranslationKey(slot.level)
                val levelText = Component.translatable("gui.estherserver.enhancement.level", slot.level)
                    .append(" (")
                    .append(Component.translatable(gradeKey))
                    .append(")")
                guiGraphics.drawString(font, levelText, startX + 24, rowY + 16, GuiTheme.TEXT_BODY)
            } else {
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.enhancement.not_owned"),
                    startX + 24, rowY + 16,
                    GuiTheme.TEXT_MUTED
                )
            }
        }
    }

    private fun renderDetailPanel(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val detailX = guiLeft + PADDING
        val detailY = guiTop + 20 + 4 * ROW_HEIGHT + 4
        val detailWidth = GUI_WIDTH - PADDING * 2
        val detailHeight = guiTop + GUI_HEIGHT - 24 - detailY

        // 디테일 패널 배경은 텍스처에 이미 포함됨

        if (selectedIndex < 0 || selectedIndex >= equipmentSlots.size) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enhancement.select_equipment"),
                guiLeft + GUI_WIDTH / 2,
                detailY + detailHeight / 2 - 4,
                GuiTheme.TEXT_MUTED
            )
            return
        }

        val slot = equipmentSlots[selectedIndex]
        val balance = EconomyClientHandler.cachedBalance

        if (slot.level < 0) {
            val buyPrice = EnhancementHandler.EQUIPMENT_BUY_PRICE
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.buy_info"),
                detailX + 6, detailY + 6,
                GuiTheme.TEXT_WHITE
            )
            val priceColor = if (balance >= buyPrice) GuiTheme.TEXT_GOLD else GuiTheme.TEXT_INSUFFICIENT
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.buy_price", buyPrice),
                detailX + 6, detailY + 18,
                priceColor
            )

            renderActionButton(
                guiGraphics, mouseX, mouseY,
                detailX + detailWidth / 2 - 35, detailY + 36, 70, 16,
                Component.translatable("gui.estherserver.enhancement.buy_button"),
                balance >= buyPrice
            )
        } else if (slot.level >= EnhancementHandler.MAX_LEVEL) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enhancement.max_level"),
                guiLeft + GUI_WIDTH / 2,
                detailY + detailHeight / 2 - 4,
                GuiTheme.GRADE_RARE
            )
        } else {
            val cost = EnhancementHandler.ENHANCEMENT_TABLE[slot.level] ?: return
            val nextLevel = slot.level + 1
            val successPercent = (cost.successRate * 100).toInt()

            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.enhance_info", slot.level, nextLevel),
                detailX + 6, detailY + 4,
                GuiTheme.TEXT_WHITE
            )

            val priceColor = if (balance >= cost.cost) GuiTheme.TEXT_GOLD else GuiTheme.TEXT_INSUFFICIENT
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.cost", cost.cost),
                detailX + 6, detailY + 16,
                priceColor
            )

            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.rate", successPercent),
                detailX + 6, detailY + 28,
                GuiTheme.TEXT_BODY
            )

            var buttonY = detailY + 42
            if (cost.requiresStone) {
                val hasStone = hasEnhancementStone()
                val stoneColor = if (hasStone) GuiTheme.GRADE_FINE else GuiTheme.TEXT_INSUFFICIENT
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.enhancement.requires_stone"),
                    detailX + 6, detailY + 40,
                    stoneColor
                )
                buttonY = detailY + 54
            }

            val canAfford = balance >= cost.cost
            val hasStone = !cost.requiresStone || hasEnhancementStone()
            renderActionButton(
                guiGraphics, mouseX, mouseY,
                detailX + detailWidth / 2 - 35, buttonY, 70, 16,
                Component.translatable("gui.estherserver.enhancement.enhance_button"),
                canAfford && hasStone
            )
        }
    }

    private fun renderActionButton(
        guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int,
        x: Int, y: Int, w: Int, h: Int,
        label: Component, enabled: Boolean
    ) {
        val isHovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h
        val bgColor = when {
            !enabled -> GuiTheme.BUTTON_DISABLED
            isHovered -> GuiTheme.BUTTON_HOVER
            else -> GuiTheme.BUTTON
        }

        guiGraphics.fill(x, y, x + w, y + h, bgColor)
        // 3D button border
        guiGraphics.fill(x, y, x + w, y + 1, GuiTheme.PANEL_BORDER_LIGHT)
        guiGraphics.fill(x, y, x + 1, y + h, GuiTheme.PANEL_BORDER_LIGHT)
        guiGraphics.fill(x, y + h - 1, x + w, y + h, GuiTheme.PANEL_BORDER_DARK)
        guiGraphics.fill(x + w - 1, y, x + w, y + h, GuiTheme.PANEL_BORDER_DARK)
        guiGraphics.drawCenteredString(font, label, x + w / 2, y + 4, GuiTheme.TEXT_WHITE)
    }

    private fun renderBalance(guiGraphics: GuiGraphics) {
        val balance = EconomyClientHandler.cachedBalance
        val balanceText = Component.translatable("gui.estherserver.shop.balance", balance)
        guiGraphics.drawCenteredString(
            font, balanceText,
            guiLeft + GUI_WIDTH / 2,
            guiTop + GUI_HEIGHT - 16,
            GuiTheme.TEXT_GOLD
        )
    }

    private fun renderTooltips(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val startX = guiLeft + PADDING
        val startY = guiTop + 20
        val rowWidth = GUI_WIDTH - PADDING * 2

        for ((index, slot) in equipmentSlots.withIndex()) {
            val rowY = startY + index * ROW_HEIGHT
            if (mouseX >= startX && mouseX < startX + rowWidth &&
                mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2
            ) {
                val tooltipLines = mutableListOf<Component>()
                tooltipLines.add(Component.translatable(slot.item.descriptionId))
                tooltipLines.add(
                    Component.translatable("gui.estherserver.enhancement.profession_label",
                        Component.translatable(slot.profession.translationKey))
                        .withStyle { it.withColor(0xAAAAAA) }
                )
                if (slot.level >= 0) {
                    tooltipLines.add(
                        Component.translatable("gui.estherserver.enhancement.level", slot.level)
                            .withStyle { it.withColor(getGradeColor(slot.level)) }
                    )
                }
                guiGraphics.renderTooltip(font, tooltipLines, Optional.empty(), mouseX, mouseY)
                break
            }
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            val startX = guiLeft + PADDING
            val startY = guiTop + 20
            val rowWidth = GUI_WIDTH - PADDING * 2

            for ((index, _) in equipmentSlots.withIndex()) {
                val rowY = startY + index * ROW_HEIGHT
                if (mouseX >= startX && mouseX < startX + rowWidth &&
                    mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2
                ) {
                    selectedIndex = index
                    return true
                }
            }

            if (selectedIndex in equipmentSlots.indices) {
                val slot = equipmentSlots[selectedIndex]
                val detailX = guiLeft + PADDING
                val detailY = guiTop + 20 + 4 * ROW_HEIGHT + 4
                val detailWidth = GUI_WIDTH - PADDING * 2
                val buttonX = detailX + detailWidth / 2 - 35
                val buttonW = 70
                val buttonH = 16

                if (slot.level < 0) {
                    val buttonY = detailY + 36
                    if (mouseX >= buttonX && mouseX < buttonX + buttonW &&
                        mouseY >= buttonY && mouseY < buttonY + buttonH
                    ) {
                        val balance = EconomyClientHandler.cachedBalance
                        if (balance >= EnhancementHandler.EQUIPMENT_BUY_PRICE) {
                            PacketDistributor.sendToServer(
                                EnhanceItemPayload(slot.profession.name, "BUY")
                            )
                            rescanTicks = 5
                        }
                        return true
                    }
                } else if (slot.level < EnhancementHandler.MAX_LEVEL) {
                    val cost = EnhancementHandler.ENHANCEMENT_TABLE[slot.level] ?: return true
                    var buttonY = detailY + 42
                    if (cost.requiresStone) buttonY = detailY + 54

                    if (mouseX >= buttonX && mouseX < buttonX + buttonW &&
                        mouseY >= buttonY && mouseY < buttonY + buttonH
                    ) {
                        val balance = EconomyClientHandler.cachedBalance
                        val canAfford = balance >= cost.cost
                        val hasStone = !cost.requiresStone || hasEnhancementStone()
                        if (canAfford && hasStone) {
                            PacketDistributor.sendToServer(
                                EnhanceItemPayload(slot.profession.name, "ENHANCE")
                            )
                            rescanTicks = 5
                        }
                        return true
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isPauseScreen(): Boolean = false

    private fun hasEnhancementStone(): Boolean {
        val player = Minecraft.getInstance().player ?: return false
        return player.inventory.items.any { !it.isEmpty && it.item === EstherServerMod.ENHANCEMENT_STONE.get() }
    }

    private fun getGradeColor(level: Int): Int = when {
        level >= 5 -> GuiTheme.GRADE_RARE
        level >= 3 -> GuiTheme.GRADE_FINE
        else -> GuiTheme.GRADE_COMMON
    }
}
