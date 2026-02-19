package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ProfessionScreen : Screen(Component.translatable("gui.estherserver.profession.title")) {

    companion object {
        private const val GUI_WIDTH = 260
        private const val GUI_HEIGHT = 230
        private const val PADDING = 10
        private const val ROW_HEIGHT = 50
        private const val BAR_HEIGHT = 6

        private val BG_COLOR = 0xFFC6C6C6.toInt()
        private val ROW_BG = 0xFF4A4A4A.toInt()
        private val BAR_BG = 0xFF222222.toInt()
        private val BAR_FILL = 0xFF55CC55.toInt()
        private val COMMON_COLOR = 0xFFFFFFFF.toInt()
        private val FINE_COLOR = 0xFF55FF55.toInt()
        private val RARE_COLOR = 0xFF5555FF.toInt()
        private val GOLD_COLOR = 0xFFFFD700.toInt()

        private val EQUIPMENT_MAP = mapOf(
            Profession.FISHING to EstherServerMod.SPECIAL_FISHING_ROD,
            Profession.FARMING to EstherServerMod.SPECIAL_HOE,
            Profession.MINING to EstherServerMod.SPECIAL_PICKAXE,
            Profession.COOKING to EstherServerMod.SPECIAL_COOKING_TOOL
        )

        private val MULTIPLIER_TABLE = mapOf(
            0 to 1.0, 1 to 1.2, 2 to 1.5, 3 to 2.0, 4 to 2.5, 5 to 3.5
        )
    }

    private var guiLeft = 0
    private var guiTop = 0

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Panel
        guiGraphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, BG_COLOR)
        guiGraphics.renderOutline(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF000000.toInt())

        // Title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.profession.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            0xFF404040.toInt()
        )

        val data = ProfessionClientHandler.cachedData
        val player = Minecraft.getInstance().player
        val startX = guiLeft + PADDING
        val contentWidth = GUI_WIDTH - PADDING * 2

        for ((index, profession) in Profession.entries.withIndex()) {
            val rowY = guiTop + 22 + index * ROW_HEIGHT

            guiGraphics.fill(startX, rowY, startX + contentWidth, rowY + ROW_HEIGHT - 2, ROW_BG)

            val level = data.getLevel(profession)
            val xp = data.getXp(profession)
            val requiredXp = if (level < Profession.MAX_LEVEL) Profession.getRequiredXp(level + 1) else 0

            // Line 1: Profession name + Level
            guiGraphics.drawString(
                font,
                Component.translatable(profession.translationKey),
                startX + 6, rowY + 4,
                GOLD_COLOR
            )
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.profession.level_display", level),
                startX + 50, rowY + 4,
                COMMON_COLOR
            )

            // Line 2: XP progress bar
            val barX = startX + 6
            val barY = rowY + 16
            val barWidth = contentWidth - 12
            guiGraphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, BAR_BG)

            if (level < Profession.MAX_LEVEL && requiredXp > 0) {
                val fillWidth = ((xp.toFloat() / requiredXp) * barWidth).toInt().coerceIn(0, barWidth)
                guiGraphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, BAR_FILL)

                // XP text
                val xpText = "$xp / $requiredXp xp"
                guiGraphics.drawString(font, xpText, barX, barY + BAR_HEIGHT + 2, 0xFFCCCCCC.toInt())
            } else {
                guiGraphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, BAR_FILL)
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.profession.max_level"),
                    barX, barY + BAR_HEIGHT + 2,
                    GOLD_COLOR
                )
            }

            // Line 3: Equipment status
            val equipItem = EQUIPMENT_MAP[profession]?.get()
            val equipY = rowY + 34

            if (player != null && equipItem != null) {
                val equipStack = findEquipment(player, equipItem)
                if (equipStack != null) {
                    val enhanceLevel = equipStack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
                    val gradeColor = getGradeColor(enhanceLevel)
                    val gradeKey = EnhancementHandler.getGradeTranslationKey(enhanceLevel)
                    val multiplier = MULTIPLIER_TABLE[enhanceLevel] ?: 1.0

                    guiGraphics.renderItem(equipStack, startX + 4, equipY - 4)
                    guiGraphics.drawString(
                        font,
                        Component.translatable("gui.estherserver.profession.equip_status",
                            enhanceLevel,
                            Component.translatable(gradeKey))
                            .append(Component.literal(" x$multiplier").withStyle { it.withColor(GOLD_COLOR) }),
                        startX + 24, equipY,
                        gradeColor
                    )
                } else {
                    guiGraphics.drawString(
                        font,
                        Component.translatable("gui.estherserver.profession.equip_none"),
                        startX + 6, equipY,
                        0xFF999999.toInt()
                    )
                }
            }
        }
    }

    override fun isPauseScreen(): Boolean = false

    private fun findEquipment(player: net.minecraft.world.entity.player.Player, item: Item): ItemStack? {
        for (i in 0 until player.inventory.items.size) {
            val stack = player.inventory.items[i]
            if (!stack.isEmpty && stack.item === item) {
                return stack
            }
        }
        return null
    }

    private fun getGradeColor(level: Int): Int = when {
        level >= 5 -> RARE_COLOR
        level >= 3 -> FINE_COLOR
        else -> COMMON_COLOR
    }
}
