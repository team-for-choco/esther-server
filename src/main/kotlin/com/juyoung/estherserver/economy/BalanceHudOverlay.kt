package com.juyoung.estherserver.economy

import com.juyoung.estherserver.Config
import com.juyoung.estherserver.EstherServerMod
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.LayeredDraw
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.VanillaGuiLayers

object BalanceHudOverlay {

    val LAYER_ID: ResourceLocation = ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "balance_hud")

    private const val BG_COLOR = 0x80000000.toInt()
    private const val TEXT_COLOR = 0xFFD700  // Gold
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
        val text = "\u2728 $balance"

        val textWidth = font.width(text)
        val textHeight = font.lineHeight

        val screenWidth = guiGraphics.guiWidth()
        val screenHeight = guiGraphics.guiHeight()

        val x = when {
            Config.balanceHudX >= 0 -> Config.balanceHudX
            else -> screenWidth + Config.balanceHudX - textWidth - PADDING_X * 2
        }
        val y = when {
            Config.balanceHudY >= 0 -> Config.balanceHudY
            else -> screenHeight + Config.balanceHudY - textHeight - PADDING_Y * 2
        }

        guiGraphics.fill(
            x, y,
            x + textWidth + PADDING_X * 2,
            y + textHeight + PADDING_Y * 2,
            BG_COLOR
        )

        guiGraphics.drawString(
            font, text,
            x + PADDING_X, y + PADDING_Y,
            TEXT_COLOR, true
        )
    }
}
