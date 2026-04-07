package com.juyoung.estherserver.enchant

import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.gui.GuiTheme
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.PacketDistributor

class EnchantMerchantScreen : Screen(Component.translatable("gui.estherserver.enchant_merchant.title")) {

    companion object {
        private const val GUI_WIDTH = 240
        private const val GUI_HEIGHT = 230
        private const val PADDING = 12
        private const val BTN_WIDTH = 106
        private const val BTN_HEIGHT = 18

        // 미리보기 도착 시 클라이언트 핸들러가 여기 저장
        var pendingPreview: List<Pair<String, Int>>? = null
    }

    private enum class ScreenState { IDLE, WAITING, PREVIEW }

    private var guiLeft = 0
    private var guiTop = 0
    private var state = ScreenState.IDLE

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        if (pendingPreview != null) state = ScreenState.PREVIEW
    }

    fun onPreviewReceived() {
        state = ScreenState.PREVIEW
    }

    fun onDone() {
        state = ScreenState.IDLE
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )

        val player = Minecraft.getInstance().player ?: return
        val item = player.mainHandItem

        if (item.isEmpty) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enchant_merchant.no_item"),
                guiLeft + GUI_WIDTH / 2,
                guiTop + 70,
                GuiTheme.TEXT_BODY
            )
        } else {
            // 아이템 아이콘 + 이름
            guiGraphics.renderItem(item, guiLeft + GUI_WIDTH / 2 - 8, guiTop + 22)
            guiGraphics.drawCenteredString(font, item.hoverName, guiLeft + GUI_WIDTH / 2, guiTop + 42, GuiTheme.TEXT_WHITE)

            // 현재 인챈트 목록
            val enchantments = item.enchantments
            val slotCount = enchantments.size()
            val enchantStartY = guiTop + 54

            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enchant_merchant.slots", slotCount),
                guiLeft + GUI_WIDTH / 2,
                enchantStartY,
                0x888888
            )

            if (slotCount == 0) {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable("gui.estherserver.enchant_merchant.no_enchants"),
                    guiLeft + GUI_WIDTH / 2,
                    enchantStartY + 11,
                    0x555555
                )
            } else {
                var yOffset = 11
                for (entry in enchantments.entrySet()) {
                    val name = entry.key.value().description()
                    val levelComp = Component.translatable("enchantment.level.${entry.intValue}")
                    val line = Component.empty().append(name).append(" ").append(levelComp)
                    guiGraphics.drawCenteredString(font, line, guiLeft + GUI_WIDTH / 2, enchantStartY + yOffset, 0xAAFFAA)
                    yOffset += 10
                    if (yOffset >= 55) break
                }
            }
        }

        // 하단 영역
        if (state == ScreenState.PREVIEW) {
            renderPreviewPanel(guiGraphics, mouseX, mouseY)
        } else {
            renderButtons(guiGraphics, mouseX, mouseY)
        }

        // 잔액
        val balance = EconomyClientHandler.cachedBalance
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.shop.balance", balance),
            guiLeft + GUI_WIDTH / 2,
            guiTop + GUI_HEIGHT - 12,
            GuiTheme.TEXT_GOLD
        )
    }

    private fun renderButtons(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val balance = EconomyClientHandler.cachedBalance
        val hasSlots = !Minecraft.getInstance().player?.mainHandItem?.let {
            it.isEmpty || it.enchantments.size() == 0
        }!!

        // 상단 행: 즉시 부여 | 선택 부여
        val row1Y = guiTop + GUI_HEIGHT - 56
        val leftX = guiLeft + PADDING
        val rightX = guiLeft + GUI_WIDTH - PADDING - BTN_WIDTH

        renderBtn(guiGraphics, mouseX, mouseY,
            leftX, row1Y, BTN_WIDTH, BTN_HEIGHT,
            Component.translatable("gui.estherserver.enchant_merchant.overwrite_btn", EnchantMerchantHandler.OVERWRITE_COST),
            canAfford = balance >= EnchantMerchantHandler.OVERWRITE_COST,
            enabled = hasSlots == true && state == ScreenState.IDLE
        )
        renderBtn(guiGraphics, mouseX, mouseY,
            rightX, row1Y, BTN_WIDTH, BTN_HEIGHT,
            Component.translatable("gui.estherserver.enchant_merchant.choose_btn", EnchantMerchantHandler.CHOOSE_COST),
            canAfford = balance >= EnchantMerchantHandler.CHOOSE_COST,
            enabled = hasSlots == true && state == ScreenState.IDLE
        )

        // 하단 행: 슬롯 추가 (중앙)
        val row2Y = row1Y + BTN_HEIGHT + 4
        val unlockX = guiLeft + GUI_WIDTH / 2 - BTN_WIDTH / 2
        val canUnlock = state == ScreenState.IDLE &&
            !(Minecraft.getInstance().player?.mainHandItem?.isEmpty ?: true) &&
            (Minecraft.getInstance().player?.mainHandItem?.enchantments?.size() ?: 0) < EnchantMerchantHandler.MAX_SLOTS
        renderBtn(guiGraphics, mouseX, mouseY,
            unlockX, row2Y, BTN_WIDTH, BTN_HEIGHT,
            Component.translatable("gui.estherserver.enchant_merchant.unlock_btn", EnchantMerchantHandler.UNLOCK_COST),
            canAfford = balance >= EnchantMerchantHandler.UNLOCK_COST,
            enabled = canUnlock
        )

        if (state == ScreenState.WAITING) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enchant_merchant.waiting"),
                guiLeft + GUI_WIDTH / 2,
                row1Y - 12,
                0xAAAAAA
            )
        }
    }

    private fun renderBtn(
        guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int,
        x: Int, y: Int, w: Int, h: Int,
        label: Component,
        canAfford: Boolean,
        enabled: Boolean
    ) {
        val hovered = enabled && mouseX in x until x + w && mouseY in y until y + h
        val bg = when {
            !enabled -> 0x88222222.toInt()
            hovered -> GuiTheme.TAB_HOVER
            else -> GuiTheme.TAB_INACTIVE
        }
        guiGraphics.fill(x, y, x + w, y + h, bg)
        val textColor = when {
            !enabled -> 0x555555
            canAfford -> GuiTheme.TEXT_WHITE
            else -> GuiTheme.TEXT_INSUFFICIENT
        }
        guiGraphics.drawCenteredString(font, label, x + w / 2, y + 5, textColor)
    }

    private fun renderPreviewPanel(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val preview = pendingPreview ?: return

        val panelX = guiLeft + PADDING
        val panelY = guiTop + GUI_HEIGHT - 90
        val panelW = GUI_WIDTH - PADDING * 2
        val panelH = 76

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC1A1A2E.toInt())
        guiGraphics.renderOutline(panelX, panelY, panelW, panelH, GuiTheme.PANEL_BORDER_LIGHT)

        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.preview_title"),
            guiLeft + GUI_WIDTH / 2,
            panelY + 4,
            GuiTheme.TEXT_TITLE
        )

        // 인챈트 목록
        var y = panelY + 15
        for ((enchantId, level) in preview.take(4)) {
            val display = getEnchantDisplayName(enchantId, level)
            guiGraphics.drawCenteredString(font, display, guiLeft + GUI_WIDTH / 2, y, 0xFFFF55)
            y += 10
        }

        // 수락 / 거절 버튼
        val btnY = panelY + panelH - 20
        val smallW = panelW / 2 - 6
        val acceptX = panelX + 4
        val declineX = panelX + panelW / 2 + 2

        val acceptHov = mouseX in acceptX until acceptX + smallW && mouseY in btnY until btnY + BTN_HEIGHT
        val declineHov = mouseX in declineX until declineX + smallW && mouseY in btnY until btnY + BTN_HEIGHT

        guiGraphics.fill(acceptX, btnY, acceptX + smallW, btnY + BTN_HEIGHT,
            if (acceptHov) 0xCC2D6A2D.toInt() else 0xCC1D4A1D.toInt())
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.estherserver.enchant_merchant.accept_btn"),
            acceptX + smallW / 2, btnY + 5, GuiTheme.TEXT_WHITE)

        guiGraphics.fill(declineX, btnY, declineX + smallW, btnY + BTN_HEIGHT,
            if (declineHov) 0xCC6A2D2D.toInt() else 0xCC4A1D1D.toInt())
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.estherserver.enchant_merchant.decline_btn"),
            declineX + smallW / 2, btnY + 5, GuiTheme.TEXT_WHITE)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val player = Minecraft.getInstance().player ?: return super.mouseClicked(mouseX, mouseY, button)
        val item = player.mainHandItem

        if (state == ScreenState.PREVIEW) {
            val preview = pendingPreview ?: return super.mouseClicked(mouseX, mouseY, button)
            val panelX = guiLeft + PADDING
            val panelY = guiTop + GUI_HEIGHT - 90
            val panelW = GUI_WIDTH - PADDING * 2
            val panelH = 76
            val btnY = panelY + panelH - 20
            val smallW = panelW / 2 - 6
            val acceptX = panelX + 4
            val declineX = panelX + panelW / 2 + 2

            if (mouseX.toInt() in acceptX until acceptX + smallW && mouseY.toInt() in btnY until btnY + BTN_HEIGHT) {
                PacketDistributor.sendToServer(EnchantConfirmPayload(true))
                pendingPreview = null
                state = ScreenState.IDLE
                return true
            }
            if (mouseX.toInt() in declineX until declineX + smallW && mouseY.toInt() in btnY until btnY + BTN_HEIGHT) {
                PacketDistributor.sendToServer(EnchantConfirmPayload(false))
                pendingPreview = null
                state = ScreenState.IDLE
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }

        if (state != ScreenState.IDLE || item.isEmpty) return super.mouseClicked(mouseX, mouseY, button)

        val slotCount = item.enchantments.size()
        val row1Y = guiTop + GUI_HEIGHT - 56
        val leftX = guiLeft + PADDING
        val rightX = guiLeft + GUI_WIDTH - PADDING - BTN_WIDTH
        val row2Y = row1Y + BTN_HEIGHT + 4
        val unlockX = guiLeft + GUI_WIDTH / 2 - BTN_WIDTH / 2

        // 즉시 부여
        if (slotCount > 0 &&
            mouseX.toInt() in leftX until leftX + BTN_WIDTH &&
            mouseY.toInt() in row1Y until row1Y + BTN_HEIGHT
        ) {
            PacketDistributor.sendToServer(EnchantRequestPayload(EnchantMode.OVERWRITE))
            state = ScreenState.WAITING
            return true
        }

        // 선택 부여
        if (slotCount > 0 &&
            mouseX.toInt() in rightX until rightX + BTN_WIDTH &&
            mouseY.toInt() in row1Y until row1Y + BTN_HEIGHT
        ) {
            PacketDistributor.sendToServer(EnchantRequestPayload(EnchantMode.CHOOSE))
            state = ScreenState.WAITING
            return true
        }

        // 슬롯 추가
        if (mouseX.toInt() in unlockX until unlockX + BTN_WIDTH &&
            mouseY.toInt() in row2Y until row2Y + BTN_HEIGHT
        ) {
            PacketDistributor.sendToServer(EnchantRequestPayload(EnchantMode.UNLOCK))
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isPauseScreen(): Boolean = false

    override fun onClose() {
        super.onClose()
        pendingPreview = null
    }

    private fun getEnchantDisplayName(enchantId: String, level: Int): Component {
        val rl = ResourceLocation.tryParse(enchantId) ?: return Component.literal(enchantId)
        val registry = Minecraft.getInstance().level
            ?.registryAccess()
            ?.lookupOrThrow(Registries.ENCHANTMENT)
            ?: return Component.literal(enchantId)
        val key = ResourceKey.create(Registries.ENCHANTMENT, rl)
        val holder = registry.get(key).orElse(null) ?: return Component.literal(enchantId)
        val name = holder.value().description()
        val levelComp = Component.translatable("enchantment.level.$level")
        return Component.empty().append(name).append(" ").append(levelComp)
    }
}
