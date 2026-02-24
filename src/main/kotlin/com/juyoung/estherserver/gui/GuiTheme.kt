package com.juyoung.estherserver.gui

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

/**
 * Esther Server GUI 공통 테마 색상 + 헬퍼.
 * 에스더의 기운 결정체 기반 시안/하늘색 톤.
 */
object GuiTheme {

    // ── 텍스처 ResourceLocations ──
    val ESTHER_ICON = ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "textures/gui/esther_icon.png")

    // ── 패널 ──
    val PANEL_BG = 0xFF2A2A3D.toInt()           // 어두운 남색
    val PANEL_BORDER_LIGHT = 0xFF5BC8F5.toInt()  // 시안 하이라이트
    val PANEL_BORDER_DARK = 0xFF1A3A5C.toInt()   // 진한 파랑
    val INNER_BG = 0xFF1E1E2E.toInt()            // 짙은 남색 (슬롯/그리드 배경)

    // ── 탭 ──
    val TAB_ACTIVE = 0xFF4DB8E8.toInt()          // 시안
    val TAB_INACTIVE = 0xFF3A3A50.toInt()        // 어두운 회남색
    val TAB_HOVER = 0xFF5BC8F5.toInt()           // 밝은 시안

    // ── 버튼 ──
    val BUTTON = 0xFF3AA0D0.toInt()              // 시안
    val BUTTON_HOVER = 0xFF5BC8F5.toInt()        // 밝은 시안
    val BUTTON_DISABLED = 0xFF2A3A4A.toInt()     // 어두운 비활성

    // ── 행/셀 ──
    val ROW_BG = 0xFF252538.toInt()              // 어두운 행 배경
    val ROW_HOVER = 0xFF303050.toInt()           // 행 호버
    val ROW_SELECTED = 0xFF1A3A5C.toInt()        // 행 선택 (진한 파랑)
    val CELL_BG = 0xFF252538.toInt()             // 셀 배경
    val CELL_HOVER = 0xFF303050.toInt()          // 셀 호버

    // ── 슬롯 ──
    val SLOT_BG = 0xFF1A3A5C.toInt()             // 슬롯 테두리
    val SLOT_INNER = 0xFF1E1E2E.toInt()          // 슬롯 내부
    val SLOT_LOCKED = 0xFF1A1A2A.toInt()         // 잠긴 슬롯
    val UNDISCOVERED_BG = 0xFF2A2A3D.toInt()     // 미발견 슬롯

    // ── 스크롤바 ──
    val SCROLLBAR_BG = 0xFF1A1A2A.toInt()
    val SCROLLBAR_THUMB = 0xFF5BC8F5.toInt()

    // ── 프로그레스 바 ──
    val BAR_BG = 0xFF1A1A2A.toInt()
    val BAR_FILL = 0xFF3AA0D0.toInt()            // 시안 (XP/진행률)
    val BAR_FILL_BRIGHT = 0xFF5BC8F5.toInt()     // 밝은 시안

    // ── 텍스트 ──
    val TEXT_WHITE = 0xFFFFFFFF.toInt()
    val TEXT_TITLE = 0xFFA0E8FF.toInt()          // 밝은 시안 제목
    val TEXT_GOLD = 0xFFFFD700.toInt()            // 화폐 골드
    val TEXT_MUTED = 0xFF888899.toInt()           // 비활성
    val TEXT_BODY = 0xFFCCCCDD.toInt()            // 본문
    val TEXT_INSUFFICIENT = 0xFFFF6666.toInt()    // 잔액 부족

    // ── 등급 ──
    val GRADE_COMMON = 0xFFFFFFFF.toInt()
    val GRADE_FINE = 0xFF55FF55.toInt()
    val GRADE_RARE = 0xFF5BC8F5.toInt()          // 시안으로 변경 (기존 파랑)

    // ── 마일스톤 ──
    val MILESTONE_ACTIVE = 0xFF1A3A5C.toInt()    // 활성 칭호 행
    val MILESTONE_UNLOCKED_HOVER = 0xFF303050.toInt()
    val MILESTONE_UNLOCKED = 0xFF252538.toInt()
    val MILESTONE_LOCKED = 0xFF1E1E2E.toInt()

    // ── HUD ──
    val HUD_BG = 0xCC1A1A2A.toInt()             // 반투명 어두운 남색

    /**
     * 바닐라 스타일 3D 입체 테두리 패널을 그린다.
     * 밝은 시안(좌/상) + 어두운 파랑(우/하) 테두리.
     */
    fun renderPanel(guiGraphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        // 외곽 밝은 시안 (상/좌)
        guiGraphics.fill(x, y, x + w, y + 1, PANEL_BORDER_LIGHT)         // top
        guiGraphics.fill(x, y, x + 1, y + h, PANEL_BORDER_LIGHT)         // left
        // 외곽 어두운 파랑 (하/우)
        guiGraphics.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER_DARK)  // bottom
        guiGraphics.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER_DARK)  // right
        // 안쪽 두 번째 테두리 (반전)
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + 2, PANEL_BORDER_DARK)    // inner top
        guiGraphics.fill(x + 1, y + 1, x + 2, y + h - 1, PANEL_BORDER_DARK)    // inner left
        guiGraphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, PANEL_BORDER_LIGHT) // inner bottom
        guiGraphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, PANEL_BORDER_LIGHT) // inner right
        // 배경 채우기
        guiGraphics.fill(x + 2, y + 2, x + w - 2, y + h - 2, PANEL_BG)
    }

    /**
     * 안쪽 어두운 영역 (슬롯 그리드, 콘텐츠 영역 등).
     * 오목한 느낌: 어두운 파랑(좌/상) + 밝은 시안(우/하).
     */
    fun renderInnerPanel(guiGraphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        // 오목 테두리: 어두운(좌/상), 밝은(우/하)
        guiGraphics.fill(x, y, x + w, y + 1, PANEL_BORDER_DARK)          // top
        guiGraphics.fill(x, y, x + 1, y + h, PANEL_BORDER_DARK)          // left
        guiGraphics.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER_LIGHT) // bottom
        guiGraphics.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER_LIGHT) // right
        // 내부
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, INNER_BG)
    }
}
