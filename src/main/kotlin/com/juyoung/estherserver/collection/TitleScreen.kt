package com.juyoung.estherserver.collection

import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.sitting.ModKeyBindings
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.PacketDistributor

class TitleScreen : Screen(Component.translatable("gui.estherserver.title.title")) {

    companion object {
        private const val GUI_WIDTH = 200
        private const val GUI_HEIGHT = 180
        private const val ROW_HEIGHT = 22
        private const val PADDING = 8
        private const val CONTENT_TOP = 28
    }

    private var guiLeft = 0
    private var guiTop = 0
    private var scroll = 0
    private var cachedUnlocked: List<Milestone> = emptyList()

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        refreshCache()
    }

    private fun refreshCache() {
        val data = CollectionClientHandler.cachedData
        cachedUnlocked = Milestone.entries.filter { it.id in data.unlockedMilestones }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // 타이틀
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.title.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 8,
            GuiTheme.TEXT_TITLE
        )

        val data = CollectionClientHandler.cachedData
        refreshCache()

        if (cachedUnlocked.isEmpty()) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.title.empty"),
                guiLeft + GUI_WIDTH / 2,
                guiTop + GUI_HEIGHT / 2 - 4,
                GuiTheme.TEXT_MUTED
            )
            renderCurrentTitle(guiGraphics, data)
            return
        }

        val contentWidth = GUI_WIDTH - PADDING * 2
        val visibleTop = guiTop + CONTENT_TOP
        val visibleHeight = GUI_HEIGHT - CONTENT_TOP - 28
        val totalHeight = cachedUnlocked.size * ROW_HEIGHT
        val maxScroll = (totalHeight - visibleHeight).coerceAtLeast(0)
        scroll = scroll.coerceIn(0, maxScroll)

        guiGraphics.enableScissor(
            guiLeft + PADDING, visibleTop,
            guiLeft + GUI_WIDTH - PADDING, visibleTop + visibleHeight
        )

        val startX = guiLeft + PADDING
        val startY = visibleTop - scroll

        for ((index, milestone) in cachedUnlocked.withIndex()) {
            val rowY = startY + index * ROW_HEIGHT
            if (rowY + ROW_HEIGHT <= visibleTop || rowY >= visibleTop + visibleHeight) continue

            val isActive = data.activeTitle == milestone.id
            val isHovered = mouseX >= startX && mouseX < startX + contentWidth &&
                mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2 &&
                mouseY >= visibleTop && mouseY < visibleTop + visibleHeight

            val rowBg = when {
                isActive -> GuiTheme.MILESTONE_ACTIVE
                isHovered -> GuiTheme.MILESTONE_UNLOCKED_HOVER
                else -> GuiTheme.MILESTONE_UNLOCKED
            }
            guiGraphics.fill(startX, rowY, startX + contentWidth, rowY + ROW_HEIGHT - 2, rowBg)

            // 칭호 이름 (색상 적용)
            val titleName = Component.translatable(milestone.titleKey).withStyle(milestone.color)
            guiGraphics.drawString(font, titleName, startX + 4, rowY + 3, GuiTheme.TEXT_WHITE)

            // 출처
            val source = Component.translatable("gui.estherserver.title.source.collection")
            guiGraphics.drawString(font, source, startX + 4, rowY + 12, GuiTheme.TEXT_MUTED)

            // 장착 표시
            if (isActive) {
                guiGraphics.drawString(font, "\u2714", startX + contentWidth - 12, rowY + 6, GuiTheme.BAR_FILL_BRIGHT)
            }
        }

        guiGraphics.disableScissor()

        // 스크롤바
        if (maxScroll > 0) {
            val scrollbarX = guiLeft + GUI_WIDTH - PADDING - 3
            val scrollbarHeight = (visibleHeight.toFloat() / totalHeight * visibleHeight).toInt().coerceAtLeast(8)
            val scrollbarY = visibleTop + (scroll.toFloat() / maxScroll * (visibleHeight - scrollbarHeight)).toInt()
            guiGraphics.fill(scrollbarX, visibleTop, scrollbarX + 3, visibleTop + visibleHeight, GuiTheme.SCROLLBAR_BG)
            guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 3, scrollbarY + scrollbarHeight, GuiTheme.SCROLLBAR_THUMB)
        }

        renderCurrentTitle(guiGraphics, data)
    }

    private fun renderCurrentTitle(guiGraphics: GuiGraphics, data: CollectionData) {
        val bottomY = guiTop + GUI_HEIGHT - 18
        val currentTitle = data.activeTitle?.let { Milestone.byId(it) }
        val text = if (currentTitle != null) {
            Component.translatable("gui.estherserver.title.current")
                .append(": ")
                .append(Component.translatable(currentTitle.titleKey).withStyle(currentTitle.color))
        } else {
            Component.translatable("gui.estherserver.title.current")
                .append(": ")
                .append(Component.translatable("gui.estherserver.title.none"))
        }
        guiGraphics.drawCenteredString(font, text, guiLeft + GUI_WIDTH / 2, bottomY, GuiTheme.TEXT_BODY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            val data = CollectionClientHandler.cachedData
            val contentWidth = GUI_WIDTH - PADDING * 2
            val startX = guiLeft + PADDING
            val visibleTop = guiTop + CONTENT_TOP
            val visibleHeight = GUI_HEIGHT - CONTENT_TOP - 28
            val startY = visibleTop - scroll

            if (mouseY >= visibleTop && mouseY < visibleTop + visibleHeight) {
                for ((index, milestone) in cachedUnlocked.withIndex()) {
                    val rowY = startY + index * ROW_HEIGHT
                    if (mouseX >= startX && mouseX < startX + contentWidth &&
                        mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2
                    ) {
                        val newId = if (data.activeTitle == milestone.id) "" else milestone.id
                        PacketDistributor.sendToServer(TitleSelectPayload(newId))
                        return true
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val visibleHeight = GUI_HEIGHT - CONTENT_TOP - 28
        val totalHeight = cachedUnlocked.size * ROW_HEIGHT
        val maxScroll = (totalHeight - visibleHeight).coerceAtLeast(0)
        if (maxScroll > 0) {
            scroll = (scroll - (scrollY * 12).toInt()).coerceIn(0, maxScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ModKeyBindings.TITLE_KEY.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean = false
}
