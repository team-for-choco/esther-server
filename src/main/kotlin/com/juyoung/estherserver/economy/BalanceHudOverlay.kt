package com.juyoung.estherserver.economy

import com.juyoung.estherserver.Config
import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.gui.GuiTheme
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.LayeredDraw
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers

object BalanceHudOverlay {

    val LAYER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "balance_hud")

    private const val ICON_SIZE = 10
    private const val ICON_GAP = 3
    private const val PADDING_X = 6
    private const val PADDING_Y = 4

    fun registerLayer(event: RegisterGuiLayersEvent) {
        event.registerAbove(VanillaGuiLayers.CHAT, LAYER_ID, LayeredDraw.Layer { guiGraphics, deltaTracker ->
            render(guiGraphics, deltaTracker)
        })
    }

    private fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        val mc = Minecraft.getInstance()
        if (mc.options.hideGui) return
        if (mc.gui.debugOverlay.showDebugScreen()) return

        val font = mc.font
        val balance = EconomyClientHandler.cachedBalance
        val balanceStr = "$balance"

        val textWidth = font.width(balanceStr)
        val textHeight = font.lineHeight
        val totalContentWidth = ICON_SIZE + ICON_GAP + textWidth

        val screenWidth = guiGraphics.guiWidth()
        val screenHeight = guiGraphics.guiHeight()

        val x = when {
            Config.balanceHudX >= 0 -> Config.balanceHudX
            else -> screenWidth + Config.balanceHudX - totalContentWidth - PADDING_X * 2
        }
        val y = when {
            Config.balanceHudY >= 0 -> Config.balanceHudY
            else -> screenHeight + Config.balanceHudY - textHeight - PADDING_Y * 2
        }

        val bgHeight = maxOf(textHeight, ICON_SIZE) + PADDING_Y * 2

        // Background
        guiGraphics.fill(x, y, x + totalContentWidth + PADDING_X * 2, y + bgHeight, GuiTheme.HUD_BG)
        // Subtle border
        guiGraphics.fill(x, y, x + totalContentWidth + PADDING_X * 2, y + 1, GuiTheme.PANEL_BORDER_DARK)
        guiGraphics.fill(x, y + bgHeight - 1, x + totalContentWidth + PADDING_X * 2, y + bgHeight, GuiTheme.PANEL_BORDER_DARK)

        val iconX = x + PADDING_X
        val iconY = y + (bgHeight - ICON_SIZE) / 2

        // Icon: blit texture if available, otherwise draw a small diamond shape
        try {
            guiGraphics.blit(RenderType::guiTextured, GuiTheme.ESTHER_ICON, iconX, iconY, 0f, 0f, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE)
        } catch (_: Exception) {
            // Fallback: draw a small diamond shape with cyan gradient
            val cx = iconX + ICON_SIZE / 2
            val cy = iconY + ICON_SIZE / 2
            guiGraphics.fill(cx - 1, iconY + 1, cx + 2, iconY + ICON_SIZE - 1, GuiTheme.PANEL_BORDER_LIGHT)
            guiGraphics.fill(iconX + 1, cy - 1, iconX + ICON_SIZE - 1, cy + 2, GuiTheme.PANEL_BORDER_LIGHT)
            guiGraphics.fill(cx, iconY + 2, cx + 1, iconY + ICON_SIZE - 2, GuiTheme.TEXT_WHITE)
            guiGraphics.fill(iconX + 2, cy, iconX + ICON_SIZE - 2, cy + 1, GuiTheme.TEXT_WHITE)
        }

        // Balance text
        guiGraphics.drawString(
            font, balanceStr,
            iconX + ICON_SIZE + ICON_GAP,
            y + (bgHeight - textHeight) / 2,
            GuiTheme.TEXT_GOLD, true
        )
    }
}
