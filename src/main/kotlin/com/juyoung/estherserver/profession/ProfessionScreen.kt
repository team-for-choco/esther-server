package com.juyoung.estherserver.profession

import com.juyoung.estherserver.sitting.ModKeyBindings
import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
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

        // Panel (260x230 영역, 512x256 캔버스)
        guiGraphics.blit(RenderType::guiTextured, GuiTheme.PROFESSION_BG, guiLeft, guiTop, 0f, 0f, GUI_WIDTH, GUI_HEIGHT, 512, 256)

        // Title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.profession.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )

        val data = ProfessionClientHandler.cachedData
        val player = Minecraft.getInstance().player
        val startX = guiLeft + PADDING
        val contentWidth = GUI_WIDTH - PADDING * 2

        for ((index, profession) in Profession.entries.withIndex()) {
            val rowY = guiTop + 22 + index * ROW_HEIGHT

            // 행 내부는 텍스처에 이미 포함됨 — 동적 콘텐츠만 렌더링

            val level = data.getLevel(profession)
            val xp = data.getXp(profession)
            val requiredXp = if (level < Profession.MAX_LEVEL) Profession.getRequiredXp(level + 1) else 0

            // Line 1: Profession name + Level
            guiGraphics.drawString(
                font,
                Component.translatable(profession.translationKey),
                startX + 6, rowY + 4,
                GuiTheme.TEXT_GOLD
            )
            guiGraphics.drawString(
                font,
                Component.translatable("gui.estherserver.profession.level_display", level),
                startX + 50, rowY + 4,
                GuiTheme.TEXT_WHITE
            )

            // Line 2: XP progress bar
            val barX = startX + 6
            val barY = rowY + 16
            val barWidth = contentWidth - 12
            guiGraphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, GuiTheme.BAR_BG)

            if (level < Profession.MAX_LEVEL && requiredXp > 0) {
                val fillWidth = ((xp.toFloat() / requiredXp) * barWidth).toInt().coerceIn(0, barWidth)
                guiGraphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, GuiTheme.BAR_FILL)

                val xpText = "$xp / $requiredXp xp"
                guiGraphics.drawString(font, xpText, barX, barY + BAR_HEIGHT + 2, GuiTheme.TEXT_BODY)
            } else {
                guiGraphics.fill(barX, barY, barX + barWidth, barY + BAR_HEIGHT, GuiTheme.BAR_FILL_BRIGHT)
                guiGraphics.drawString(
                    font,
                    Component.translatable("gui.estherserver.profession.max_level"),
                    barX, barY + BAR_HEIGHT + 2,
                    GuiTheme.TEXT_GOLD
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
                            .append(Component.literal(" x$multiplier").withStyle { it.withColor(GuiTheme.TEXT_GOLD) }),
                        startX + 24, equipY,
                        gradeColor
                    )
                } else {
                    guiGraphics.drawString(
                        font,
                        Component.translatable("gui.estherserver.profession.equip_none"),
                        startX + 6, equipY,
                        GuiTheme.TEXT_MUTED
                    )
                }
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ModKeyBindings.PROFESSION_KEY.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
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
        level >= 5 -> GuiTheme.GRADE_RARE
        level >= 3 -> GuiTheme.GRADE_FINE
        else -> GuiTheme.GRADE_COMMON
    }
}
