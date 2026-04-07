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
        private const val GUI_HEIGHT = 210
        private const val PADDING = 12

        private const val BTN_WIDTH = 100
        private const val BTN_HEIGHT = 18

        const val OVERWRITE_COST = EnchantMerchantHandler.OVERWRITE_COST
        const val CHOOSE_COST = EnchantMerchantHandler.CHOOSE_COST

        // Called by client handler when preview arrives
        var pendingPreview: Pair<String, Int>? = null // enchantId, level
    }

    private enum class ScreenState { IDLE, WAITING, PREVIEW }

    private var guiLeft = 0
    private var guiTop = 0
    private var state = ScreenState.IDLE

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
        // If a preview arrived before we rendered, pick it up
        if (pendingPreview != null) {
            state = ScreenState.PREVIEW
        }
    }

    fun onPreviewReceived() {
        state = ScreenState.PREVIEW
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // Title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.title"),
            guiLeft + GUI_WIDTH / 2,
            guiTop + 6,
            GuiTheme.TEXT_TITLE
        )

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val item = player.mainHandItem

        if (item.isEmpty) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enchant_merchant.no_item"),
                guiLeft + GUI_WIDTH / 2,
                guiTop + 60,
                GuiTheme.TEXT_BODY
            )
        } else {
            // Item icon
            val itemX = guiLeft + GUI_WIDTH / 2 - 8
            val itemY = guiTop + 22
            guiGraphics.renderItem(item, itemX, itemY)

            // Item name
            guiGraphics.drawCenteredString(
                font,
                item.hoverName,
                guiLeft + GUI_WIDTH / 2,
                guiTop + 42,
                GuiTheme.TEXT_WHITE
            )

            // Current enchantments
            val enchantments = item.enchantments
            val enchantY = guiTop + 54
            if (enchantments.isEmpty) {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable("gui.estherserver.enchant_merchant.no_enchants"),
                    guiLeft + GUI_WIDTH / 2,
                    enchantY,
                    0x888888
                )
            } else {
                var yOffset = 0
                for (entry in enchantments.entrySet()) {
                    val enchantName = entry.key.value().description()
                    val levelComp = Component.translatable("enchantment.level.${entry.intValue}")
                    val line = Component.empty().append(enchantName).append(" ").append(levelComp)
                    guiGraphics.drawCenteredString(font, line, guiLeft + GUI_WIDTH / 2, enchantY + yOffset, 0xAAFFAA)
                    yOffset += 10
                    if (yOffset >= 40) break // max 4 enchants displayed
                }
            }
        }

        // Preview panel (PREVIEW state)
        if (state == ScreenState.PREVIEW) {
            renderPreviewPanel(guiGraphics, mouseX, mouseY)
        } else {
            // Buttons
            renderButtons(guiGraphics, mouseX, mouseY)
        }

        // Balance
        val balance = EconomyClientHandler.cachedBalance
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.shop.balance", balance),
            guiLeft + GUI_WIDTH / 2,
            guiTop + GUI_HEIGHT - 14,
            GuiTheme.TEXT_GOLD
        )
    }

    private fun renderButtons(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val balance = EconomyClientHandler.cachedBalance
        val centerX = guiLeft + GUI_WIDTH / 2

        val btnY = guiTop + GUI_HEIGHT - 50

        // Overwrite button (left)
        val overwBtn = getBtnOverwriteBounds()
        val overwHovered = isInBounds(mouseX, mouseY, overwBtn)
        val overwColor = if (overwHovered && state == ScreenState.IDLE) GuiTheme.TAB_HOVER else GuiTheme.TAB_INACTIVE
        guiGraphics.fill(overwBtn.first, btnY, overwBtn.first + BTN_WIDTH, btnY + BTN_HEIGHT, overwColor)
        val overwText = Component.translatable("gui.estherserver.enchant_merchant.overwrite_btn", OVERWRITE_COST)
        val overwColor2 = if (balance >= OVERWRITE_COST) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_INSUFFICIENT
        guiGraphics.drawCenteredString(font, overwText, overwBtn.first + BTN_WIDTH / 2, btnY + 5, overwColor2)

        // Choose button (right)
        val chooseBtn = getBtnChooseBounds()
        val chooseHovered = isInBounds(mouseX, mouseY, chooseBtn)
        val chooseColor = if (chooseHovered && state == ScreenState.IDLE) GuiTheme.TAB_HOVER else GuiTheme.TAB_INACTIVE
        guiGraphics.fill(chooseBtn.first, btnY, chooseBtn.first + BTN_WIDTH, btnY + BTN_HEIGHT, chooseColor)
        val chooseText = Component.translatable("gui.estherserver.enchant_merchant.choose_btn", CHOOSE_COST)
        val chooseColor2 = if (balance >= CHOOSE_COST) GuiTheme.TEXT_WHITE else GuiTheme.TEXT_INSUFFICIENT
        guiGraphics.drawCenteredString(font, chooseText, chooseBtn.first + BTN_WIDTH / 2, btnY + 5, chooseColor2)

        // Waiting indicator
        if (state == ScreenState.WAITING) {
            guiGraphics.drawCenteredString(
                font,
                Component.translatable("gui.estherserver.enchant_merchant.waiting"),
                guiLeft + GUI_WIDTH / 2,
                btnY - 12,
                0xAAAAAA
            )
        }
    }

    private fun renderPreviewPanel(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val preview = pendingPreview ?: return
        val (enchantId, level) = preview

        val panelX = guiLeft + PADDING
        val panelY = guiTop + GUI_HEIGHT - 72
        val panelW = GUI_WIDTH - PADDING * 2
        val panelH = 58

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC1A1A2E.toInt())
        guiGraphics.renderOutline(panelX, panelY, panelW, panelH, GuiTheme.PANEL_BORDER_LIGHT)

        // New enchant title
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.preview_title"),
            guiLeft + GUI_WIDTH / 2,
            panelY + 4,
            GuiTheme.TEXT_TITLE
        )

        // Enchant name
        val enchantDisplay = getEnchantDisplayName(enchantId, level)
        guiGraphics.drawCenteredString(
            font, enchantDisplay,
            guiLeft + GUI_WIDTH / 2,
            panelY + 15,
            0xFFFF55
        )

        // Accept / Decline buttons
        val acceptX = panelX + 4
        val declineX = panelX + panelW / 2 + 2
        val btnY = panelY + 28
        val smallBtnW = panelW / 2 - 6

        val acceptHovered = mouseX in acceptX until acceptX + smallBtnW && mouseY in btnY until btnY + BTN_HEIGHT
        val declineHovered = mouseX in declineX until declineX + smallBtnW && mouseY in btnY until btnY + BTN_HEIGHT

        guiGraphics.fill(acceptX, btnY, acceptX + smallBtnW, btnY + BTN_HEIGHT,
            if (acceptHovered) 0xCC2D6A2D.toInt() else 0xCC1D4A1D.toInt())
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.accept_btn", CHOOSE_COST),
            acceptX + smallBtnW / 2, btnY + 5, GuiTheme.TEXT_WHITE
        )

        guiGraphics.fill(declineX, btnY, declineX + smallBtnW, btnY + BTN_HEIGHT,
            if (declineHovered) 0xCC6A2D2D.toInt() else 0xCC4A1D1D.toInt())
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("gui.estherserver.enchant_merchant.decline_btn"),
            declineX + smallBtnW / 2, btnY + 5, GuiTheme.TEXT_WHITE
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button)

        val mc = Minecraft.getInstance()
        val player = mc.player ?: return super.mouseClicked(mouseX, mouseY, button)
        val item = player.mainHandItem

        if (state == ScreenState.PREVIEW) {
            val preview = pendingPreview ?: return super.mouseClicked(mouseX, mouseY, button)
            val (_, _) = preview

            val panelX = guiLeft + PADDING
            val panelY = guiTop + GUI_HEIGHT - 72
            val panelW = GUI_WIDTH - PADDING * 2
            val acceptX = panelX + 4
            val declineX = panelX + panelW / 2 + 2
            val btnY = panelY + 28
            val smallBtnW = panelW / 2 - 6

            if (mouseX.toInt() in acceptX until acceptX + smallBtnW &&
                mouseY.toInt() in btnY until btnY + BTN_HEIGHT
            ) {
                PacketDistributor.sendToServer(EnchantConfirmPayload(true))
                pendingPreview = null
                state = ScreenState.IDLE
                return true
            }

            if (mouseX.toInt() in declineX until declineX + smallBtnW &&
                mouseY.toInt() in btnY until btnY + BTN_HEIGHT
            ) {
                PacketDistributor.sendToServer(EnchantConfirmPayload(false))
                pendingPreview = null
                state = ScreenState.IDLE
                return true
            }

            return super.mouseClicked(mouseX, mouseY, button)
        }

        if (state == ScreenState.IDLE && !item.isEmpty) {
            val btnY = guiTop + GUI_HEIGHT - 50

            val overwBtn = getBtnOverwriteBounds()
            if (mouseX.toInt() in overwBtn.first until overwBtn.first + BTN_WIDTH &&
                mouseY.toInt() in btnY until btnY + BTN_HEIGHT
            ) {
                PacketDistributor.sendToServer(EnchantRequestPayload("OVERWRITE"))
                state = ScreenState.WAITING
                return true
            }

            val chooseBtn = getBtnChooseBounds()
            if (mouseX.toInt() in chooseBtn.first until chooseBtn.first + BTN_WIDTH &&
                mouseY.toInt() in btnY until btnY + BTN_HEIGHT
            ) {
                PacketDistributor.sendToServer(EnchantRequestPayload("CHOOSE"))
                state = ScreenState.WAITING
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isPauseScreen(): Boolean = false

    private fun getBtnOverwriteBounds(): Pair<Int, Int> {
        val centerX = guiLeft + GUI_WIDTH / 2
        return Pair(centerX - BTN_WIDTH - 4, guiTop + GUI_HEIGHT - 50)
    }

    private fun getBtnChooseBounds(): Pair<Int, Int> {
        val centerX = guiLeft + GUI_WIDTH / 2
        return Pair(centerX + 4, guiTop + GUI_HEIGHT - 50)
    }

    private fun isInBounds(mouseX: Int, mouseY: Int, btn: Pair<Int, Int>): Boolean {
        val btnY = guiTop + GUI_HEIGHT - 50
        return mouseX in btn.first until btn.first + BTN_WIDTH &&
            mouseY in btnY until btnY + BTN_HEIGHT
    }

    private fun getEnchantDisplayName(enchantId: String, level: Int): Component {
        val rl = ResourceLocation.tryParse(enchantId)
            ?: return Component.literal(enchantId)
        val registry = Minecraft.getInstance().level
            ?.registryAccess()
            ?.lookupOrThrow(Registries.ENCHANTMENT)
            ?: return Component.literal(enchantId)
        val key = ResourceKey.create(Registries.ENCHANTMENT, rl)
        val holder = registry.get(key).orElse(null)
            ?: return Component.literal(enchantId)
        val name = holder.value().description()
        val levelComp = Component.translatable("enchantment.level.$level")
        return Component.empty().append(name).append(" ").append(levelComp)
    }
}
