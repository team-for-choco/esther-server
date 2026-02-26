package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.gui.GuiTheme
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class GachaRouletteScreen : Screen(Component.translatable("gui.estherserver.gacha_roulette.title")) {

    companion object {
        private const val GUI_WIDTH = 260
        private const val GUI_HEIGHT = 110
        private const val ITEM_RENDER_SIZE = 16 // renderItem 실제 렌더 크기
        private const val HIGHLIGHT_SIZE = 20   // 하이라이트 테두리 크기
        private const val SLOT_SPACING = 28
        private const val STRIP_HEIGHT = 32
        private const val REEL_COUNT = 40
        private const val ANIM_TICKS = 60 // ~3 seconds
        private const val MARKER_COLOR = 0xFFFFD700.toInt()
        private const val WINNER_BORDER = 0xFFFFD700.toInt()
    }

    private var guiLeft = 0
    private var guiTop = 0

    // 릴 시퀀스: 풀 아이템을 랜덤으로 배열, 마지막 중앙에 당첨 아이템
    private var reelEntries = listOf<RouletteDisplayEntry>()
    private var reelStacks = listOf<ItemStack>()
    private var winnerReelIndex = 0

    // 애니메이션 상태
    private var animTick = 0
    private var animFinished = false
    private var resultMessageSent = false
    private var scrollOffset = 0f
    private var targetOffset = 0f

    // 결과 정보
    private var winnerEntry: RouletteDisplayEntry? = null
    private var poolTitle = ""

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2

        val payload = GachaClientHandler.cachedPayload ?: return

        poolTitle = when {
            payload.poolId.contains("pet") -> Component.translatable("gui.estherserver.gacha_roulette.title_pet").string
            payload.poolId.contains("furniture") -> Component.translatable("gui.estherserver.gacha_roulette.title_furniture").string
            else -> Component.translatable("gui.estherserver.gacha_roulette.title").string
        }

        winnerEntry = payload.entries.getOrNull(payload.winnerIndex)

        // 릴 시퀀스 빌드: REEL_COUNT 개의 랜덤 엔트리 + 당첨 아이템이 중앙에 오도록
        val poolEntries = payload.entries
        val reel = mutableListOf<RouletteDisplayEntry>()
        val random = java.util.Random()

        // 앞 부분: 랜덤 채우기
        for (i in 0 until REEL_COUNT) {
            reel.add(poolEntries[random.nextInt(poolEntries.size)])
        }

        // 당첨 아이템을 릴의 특정 위치에 배치 (스트립 중앙에 멈출 위치)
        winnerReelIndex = REEL_COUNT - 5 // 마지막 근처에 배치
        if (winnerEntry != null) {
            reel[winnerReelIndex] = winnerEntry!!
        }

        reelEntries = reel
        reelStacks = reel.map { entry -> resolveItemStack(entry) }

        // 타겟 오프셋: 당첨 아이템이 스트립 중앙에 오도록
        targetOffset = (winnerReelIndex * SLOT_SPACING).toFloat()

        animTick = 0
        animFinished = false
        resultMessageSent = false
        scrollOffset = 0f
    }

    private fun resolveItemStack(entry: RouletteDisplayEntry): ItemStack {
        return try {
            val rl = ResourceLocation.parse(entry.itemId)
            val item = BuiltInRegistries.ITEM.getValue(rl)
            ItemStack(item, entry.count.coerceAtLeast(1))
        } catch (_: Exception) {
            ItemStack.EMPTY
        }
    }

    override fun tick() {
        super.tick()
        if (animFinished) return

        animTick++
        if (animTick >= ANIM_TICKS) {
            animTick = ANIM_TICKS
            animFinished = true
            scrollOffset = targetOffset
            sendResultChatMessage()
        } else {
            // ease-out-cubic: 1 - (1 - t)^3
            val t = animTick.toFloat() / ANIM_TICKS.toFloat()
            val eased = 1f - (1f - t) * (1f - t) * (1f - t)
            scrollOffset = targetOffset * eased
        }
    }

    private fun sendResultChatMessage() {
        if (resultMessageSent) return
        resultMessageSent = true

        val entry = winnerEntry ?: return
        val player = Minecraft.getInstance().player ?: return

        val message = if (entry.isCurrency) {
            Component.translatable(
                "message.estherserver.gacha_result_currency",
                entry.currencyAmount
            )
        } else {
            val displayName = Component.translatable(entry.displayKey)
            Component.translatable("message.estherserver.gacha_result_item", displayName)
        }
        player.displayClientMessage(message, false)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // 부드러운 보간
        val displayOffset = if (!animFinished && animTick < ANIM_TICKS) {
            val nextTick = (animTick + 1).coerceAtMost(ANIM_TICKS)
            val tCurr = animTick.toFloat() / ANIM_TICKS.toFloat()
            val tNext = nextTick.toFloat() / ANIM_TICKS.toFloat()
            val easedCurr = 1f - (1f - tCurr) * (1f - tCurr) * (1f - tCurr)
            val easedNext = 1f - (1f - tNext) * (1f - tNext) * (1f - tNext)
            val currOffset = targetOffset * easedCurr
            val nextOffset = targetOffset * easedNext
            currOffset + (nextOffset - currOffset) * partialTick
        } else {
            targetOffset
        }

        // 패널 배경
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // 제목
        guiGraphics.drawCenteredString(
            font, poolTitle,
            guiLeft + GUI_WIDTH / 2, guiTop + 8, GuiTheme.TEXT_TITLE
        )

        // 스트립 영역
        val stripLeft = guiLeft + 10
        val stripRight = guiLeft + GUI_WIDTH - 10
        val stripTop = guiTop + 24
        val stripBottom = stripTop + STRIP_HEIGHT + 4

        // 스트립 배경 (오목 패널)
        GuiTheme.renderInnerPanel(guiGraphics, stripLeft, stripTop, stripRight - stripLeft, stripBottom - stripTop)

        // 중앙 마커 (삼각형 표시를 선 한 줄로 대체)
        val markerX = (stripLeft + stripRight) / 2
        guiGraphics.fill(markerX - 1, stripTop - 2, markerX + 1, stripTop, MARKER_COLOR)
        guiGraphics.fill(markerX - 1, stripBottom, markerX + 1, stripBottom + 2, MARKER_COLOR)

        // 클리핑 영역 내 아이콘 렌더링
        val clipX = stripLeft + 2
        val clipY = stripTop + 2
        val clipW = stripRight - stripLeft - 4
        val clipH = stripBottom - stripTop - 4

        // 스트립의 정확한 중앙 (마커와 동일)
        val centerX = markerX.toFloat()
        val iconY = clipY + (clipH - ITEM_RENDER_SIZE) / 2

        guiGraphics.enableScissor(clipX, clipY, clipX + clipW, clipY + clipH)

        for (i in reelEntries.indices) {
            val stack = reelStacks.getOrNull(i) ?: continue

            // 슬롯 i의 중앙 X 좌표
            val slotCenterX = centerX + (i * SLOT_SPACING) - displayOffset
            // 아이콘 렌더 위치 (16x16 기준 중앙 정렬)
            val renderX = (slotCenterX - ITEM_RENDER_SIZE / 2f).toInt()

            // 화면 밖이면 스킵
            if (renderX + ITEM_RENDER_SIZE < clipX || renderX > clipX + clipW) continue

            // 당첨 아이템 하이라이트 (애니메이션 완료 후)
            if (animFinished && i == winnerReelIndex) {
                val hlX = (slotCenterX - HIGHLIGHT_SIZE / 2f).toInt()
                val hlY = iconY + (ITEM_RENDER_SIZE - HIGHLIGHT_SIZE) / 2
                guiGraphics.fill(
                    hlX - 1, hlY - 1,
                    hlX + HIGHLIGHT_SIZE + 1, hlY + HIGHLIGHT_SIZE + 1,
                    WINNER_BORDER
                )
                guiGraphics.fill(
                    hlX, hlY,
                    hlX + HIGHLIGHT_SIZE, hlY + HIGHLIGHT_SIZE,
                    GuiTheme.INNER_BG
                )
            }

            guiGraphics.renderItem(stack, renderX, iconY)

            // 수량 표시
            val entry = reelEntries[i]
            if (!entry.isCurrency && entry.count > 1) {
                guiGraphics.renderItemDecorations(font, stack, renderX, iconY)
            }
        }

        guiGraphics.disableScissor()

        // 세로 마커 라인 (스트립 위에)
        guiGraphics.fill(markerX - 1, clipY, markerX + 1, clipY + clipH, MARKER_COLOR)

        // 결과 텍스트 (애니메이션 완료 후)
        if (animFinished && winnerEntry != null) {
            val entry = winnerEntry!!
            val resultText = if (entry.isCurrency) {
                Component.translatable("gui.estherserver.gacha_roulette.result_currency", entry.currencyAmount)
            } else {
                val name = Component.translatable(entry.displayKey)
                if (entry.count > 1) {
                    Component.translatable("gui.estherserver.gacha_roulette.result_item_count", name, entry.count)
                } else {
                    Component.translatable("gui.estherserver.gacha_roulette.result_item", name)
                }
            }
            guiGraphics.drawCenteredString(
                font, resultText,
                guiLeft + GUI_WIDTH / 2, stripBottom + 8, GuiTheme.TEXT_GOLD
            )

            // 닫기 안내
            guiGraphics.drawCenteredString(
                font, Component.translatable("gui.estherserver.gacha_roulette.close_hint"),
                guiLeft + GUI_WIDTH / 2, stripBottom + 22, GuiTheme.TEXT_MUTED
            )
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (animFinished) {
            onClose()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (animFinished) {
            onClose()
            return true
        }
        // ESC는 항상 닫기 가능
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen() = false
}
