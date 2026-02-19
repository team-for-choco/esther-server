package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
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

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val ROW_BG = 0xFF4A4A4A.toInt()
        private val ROW_HOVER = 0xFF5A5A5A.toInt()
        private val ROW_SELECTED = 0xFF3A5A3A.toInt()
        private val GOLD_COLOR = 0xFFFFD700.toInt()
        private val INSUFFICIENT_COLOR = 0xFFFF6666.toInt()
        private val COMMON_COLOR = 0xFFFFFFFF.toInt()
        private val FINE_COLOR = 0xFF55FF55.toInt()
        private val RARE_COLOR = 0xFF5555FF.toInt()
        private val BUTTON_COLOR = 0xFF55AA55.toInt()
        private val BUTTON_HOVER = 0xFF66BB66.toInt()
        private val BUTTON_DISABLED = 0xFF888888.toInt()
        private val DETAIL_BG = 0xFF3A3A3A.toInt()
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
            var found: ItemStack? = null
            for (i in 0 until player.inventory.items.size) {
                val stack = player.inventory.items[i]
                if (!stack.isEmpty && stack.item === item) {
                    found = stack
                    break
                }
            }

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
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())
    }

    private fun renderTitle(guiGraphics: GuiGraphics) {
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enhancement.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            0xFF404040.toInt()
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
                isSelected -> ROW_SELECTED
                isHovered -> ROW_HOVER
                else -> ROW_BG
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
            val nameColor = if (slot.level >= 0) getGradeColor(slot.level) else 0xFFAAAAAA.toInt()
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
                guiGraphics.drawString(font, levelText, startX + 24, rowY + 16, 0xFFCCCCCC.toInt())
            } else {
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.enhancement.not_owned"),
                    startX + 24, rowY + 16,
                    0xFF999999.toInt()
                )
            }
        }
    }

    private fun renderDetailPanel(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val detailX = guiLeft + PADDING
        val detailY = guiTop + 20 + 4 * ROW_HEIGHT + 4
        val detailWidth = GUI_WIDTH - PADDING * 2
        val detailHeight = guiTop + GUI_HEIGHT - 24 - detailY

        guiGraphics.fill(detailX, detailY, detailX + detailWidth, detailY + detailHeight, DETAIL_BG)
        guiGraphics.renderOutline(detailX, detailY, detailWidth, detailHeight, 0xFF000000.toInt())

        if (selectedIndex < 0 || selectedIndex >= equipmentSlots.size) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enhancement.select_equipment"),
                guiLeft + GUI_WIDTH / 2,
                detailY + detailHeight / 2 - 4,
                0xFF999999.toInt()
            )
            return
        }

        val slot = equipmentSlots[selectedIndex]
        val balance = EconomyClientHandler.cachedBalance

        if (slot.level < 0) {
            // Not owned - show buy info
            val buyPrice = EnhancementHandler.EQUIPMENT_BUY_PRICE
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.buy_info"),
                detailX + 6, detailY + 6,
                COMMON_COLOR
            )
            val priceColor = if (balance >= buyPrice) GOLD_COLOR else INSUFFICIENT_COLOR
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.buy_price", buyPrice),
                detailX + 6, detailY + 18,
                priceColor
            )

            // Buy button
            renderActionButton(
                guiGraphics, mouseX, mouseY,
                detailX + detailWidth / 2 - 35, detailY + 36, 70, 16,
                Component.translatable("gui.estherserver.enhancement.buy_button"),
                balance >= buyPrice
            )
        } else if (slot.level >= EnhancementHandler.MAX_LEVEL) {
            // Max level
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enhancement.max_level"),
                guiLeft + GUI_WIDTH / 2,
                detailY + detailHeight / 2 - 4,
                RARE_COLOR
            )
        } else {
            // Can enhance
            val cost = EnhancementHandler.ENHANCEMENT_TABLE[slot.level] ?: return
            val nextLevel = slot.level + 1
            val successPercent = (cost.successRate * 100).toInt()

            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.enhancement.enhance_info", slot.level, nextLevel),
                detailX + 6, detailY + 4,
                COMMON_COLOR
            )

            val priceColor = if (balance >= cost.cost) GOLD_COLOR else INSUFFICIENT_COLOR
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
                0xFFCCCCCC.toInt()
            )

            var buttonY = detailY + 42
            if (cost.requiresStone) {
                val hasStone = hasEnhancementStone()
                val stoneColor = if (hasStone) FINE_COLOR else INSUFFICIENT_COLOR
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
            !enabled -> BUTTON_DISABLED
            isHovered -> BUTTON_HOVER
            else -> BUTTON_COLOR
        }

        guiGraphics.fill(x, y, x + w, y + h, bgColor)
        guiGraphics.renderOutline(x, y, w, h, 0xFF000000.toInt())
        guiGraphics.drawCenteredString(font, label, x + w / 2, y + 4, COMMON_COLOR)
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
            // Equipment row click
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

            // Action button click
            if (selectedIndex in equipmentSlots.indices) {
                val slot = equipmentSlots[selectedIndex]
                val detailX = guiLeft + PADDING
                val detailY = guiTop + 20 + 4 * ROW_HEIGHT + 4
                val detailWidth = GUI_WIDTH - PADDING * 2
                val buttonX = detailX + detailWidth / 2 - 35
                val buttonW = 70
                val buttonH = 16

                if (slot.level < 0) {
                    // Buy button
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
                    // Enhance button
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
        for (i in 0 until player.inventory.items.size) {
            val stack = player.inventory.items[i]
            if (!stack.isEmpty && stack.item === EstherServerMod.ENHANCEMENT_STONE.get()) {
                return true
            }
        }
        return false
    }

    private fun getGradeColor(level: Int): Int = when {
        level >= 5 -> RARE_COLOR
        level >= 3 -> FINE_COLOR
        else -> COMMON_COLOR
    }
}
