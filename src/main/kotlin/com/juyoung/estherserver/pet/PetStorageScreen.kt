package com.juyoung.estherserver.pet

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.sitting.ModKeyBindings
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Quaternionf
import org.joml.Vector3f

@OnlyIn(Dist.CLIENT)
class PetStorageScreen : Screen(Component.translatable("gui.estherserver.pet_storage.title")) {

    private var guiLeft = 0
    private var guiTop = 0
    private var selectedIndex = -1
    private var previewEntity: PetEntity? = null

    companion object {
        private const val GUI_WIDTH = 280
        private const val GUI_HEIGHT = 200

        // Left panel — pet grid
        private const val GRID_LEFT = 10
        private const val GRID_TOP = 24
        private const val SLOT_SIZE = 36
        private const val SLOT_GAP = 4
        private const val SLOTS_PER_ROW = 2
        private const val MAX_SLOTS = 8
        private const val LEFT_PANEL_WIDTH = 92

        // Right panel — detail
        private const val DETAIL_LEFT = 106
        private const val DETAIL_TOP = 24

        // Button
        private const val BTN_WIDTH = 80
        private const val BTN_HEIGHT = 16
    }

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2

        // Auto-select first pet or summoned pet
        val pets = PetClientHandler.cachedOwnedPets
        val summoned = PetClientHandler.cachedSummonedPet
        selectedIndex = if (summoned != null) {
            pets.indexOf(summoned).coerceAtLeast(0)
        } else if (pets.isNotEmpty()) {
            0
        } else {
            -1
        }

        updatePreviewEntity()
    }

    private fun updatePreviewEntity() {
        val pets = PetClientHandler.cachedOwnedPets
        if (selectedIndex in pets.indices) {
            val mc = Minecraft.getInstance()
            val level = mc.level ?: return
            val entity = PetEntity(EstherServerMod.PET_ENTITY.get(), level)
            entity.petType = pets[selectedIndex]
            previewEntity = entity
        } else {
            previewEntity = null
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        val pets = PetClientHandler.cachedOwnedPets
        val summonedPet = PetClientHandler.cachedSummonedPet

        // Main panel
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // Title
        guiGraphics.drawCenteredString(font, title, guiLeft + GUI_WIDTH / 2, guiTop + 8, GuiTheme.TEXT_TITLE)

        // ── Left panel: Pet grid ──
        renderPetGrid(guiGraphics, pets, summonedPet, mouseX, mouseY)

        // ── Divider line ──
        val divX = guiLeft + LEFT_PANEL_WIDTH + 6
        guiGraphics.fill(divX, guiTop + GRID_TOP, divX + 1, guiTop + GUI_HEIGHT - 14, GuiTheme.PANEL_BORDER_DARK)

        // ── Right panel: Detail ──
        renderDetailPanel(guiGraphics, pets, summonedPet, mouseX, mouseY)
    }

    private fun renderPetGrid(
        guiGraphics: GuiGraphics,
        pets: List<PetType>,
        summonedPet: PetType?,
        mouseX: Int,
        mouseY: Int
    ) {
        for (i in 0 until MAX_SLOTS) {
            val col = i % SLOTS_PER_ROW
            val row = i / SLOTS_PER_ROW
            val slotX = guiLeft + GRID_LEFT + col * (SLOT_SIZE + SLOT_GAP)
            val slotY = guiTop + GRID_TOP + row * (SLOT_SIZE + SLOT_GAP)

            val isHovered = mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY < slotY + SLOT_SIZE
            val isSelected = i == selectedIndex

            if (i < pets.size) {
                val pet = pets[i]
                val isSummoned = pet == summonedPet

                // Slot background
                val bg = when {
                    isSelected -> GuiTheme.ROW_SELECTED
                    isHovered -> GuiTheme.CELL_HOVER
                    else -> GuiTheme.CELL_BG
                }
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, bg)

                // Grade border (2px for selected)
                val borderColor = pet.grade.color
                val bw = if (isSelected) 2 else 1
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + bw, borderColor)
                guiGraphics.fill(slotX, slotY + SLOT_SIZE - bw, slotX + SLOT_SIZE, slotY + SLOT_SIZE, borderColor)
                guiGraphics.fill(slotX, slotY, slotX + bw, slotY + SLOT_SIZE, borderColor)
                guiGraphics.fill(slotX + SLOT_SIZE - bw, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, borderColor)

                // Mini entity preview in slot
                renderMiniEntity(guiGraphics, pet, slotX, slotY)

                // Pet name below entity
                val name = Component.translatable(pet.displayKey)
                guiGraphics.drawCenteredString(font, name, slotX + SLOT_SIZE / 2, slotY + SLOT_SIZE - 10, GuiTheme.TEXT_WHITE)

                // Summoned indicator dot
                if (isSummoned) {
                    guiGraphics.fill(slotX + SLOT_SIZE - 6, slotY + 2, slotX + SLOT_SIZE - 2, slotY + 6, GuiTheme.TEXT_GOLD)
                }
            } else {
                // Empty locked slot
                GuiTheme.renderInnerPanel(guiGraphics, slotX, slotY, SLOT_SIZE, SLOT_SIZE)
            }
        }
    }

    private fun renderMiniEntity(guiGraphics: GuiGraphics, pet: PetType, slotX: Int, slotY: Int) {
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val entity = PetEntity(EstherServerMod.PET_ENTITY.get(), level)
        entity.petType = pet

        val centerX = slotX + SLOT_SIZE / 2
        val centerY = slotY + SLOT_SIZE - 16

        try {
            InventoryScreen.renderEntityInInventory(
                guiGraphics,
                centerX.toFloat(),
                centerY.toFloat(),
                20f,
                Vector3f(0f, 0f, 0f),
                Quaternionf().rotateY(Math.toRadians(210.0).toFloat()),
                null,
                entity
            )
        } catch (_: Exception) {
            // Fallback: draw colored square
            guiGraphics.fill(slotX + 8, slotY + 4, slotX + SLOT_SIZE - 8, slotY + SLOT_SIZE - 14, pet.grade.color)
        }
    }

    private fun renderDetailPanel(
        guiGraphics: GuiGraphics,
        pets: List<PetType>,
        summonedPet: PetType?,
        mouseX: Int,
        mouseY: Int
    ) {
        val detailX = guiLeft + DETAIL_LEFT
        val detailY = guiTop + DETAIL_TOP

        if (selectedIndex !in pets.indices) {
            // No pet selected
            val msg = Component.translatable("gui.estherserver.pet_storage.select")
            guiGraphics.drawCenteredString(
                font, msg,
                detailX + (GUI_WIDTH - DETAIL_LEFT - 10) / 2,
                detailY + 60,
                GuiTheme.TEXT_MUTED
            )
            return
        }

        val pet = pets[selectedIndex]
        val isSummoned = pet == summonedPet
        val panelWidth = GUI_WIDTH - DETAIL_LEFT - 10

        // ── Entity preview area ──
        val previewAreaX = detailX
        val previewAreaY = detailY
        val previewAreaW = panelWidth
        val previewAreaH = 80
        GuiTheme.renderInnerPanel(guiGraphics, previewAreaX, previewAreaY, previewAreaW, previewAreaH)

        // Render entity preview
        val entity = previewEntity
        if (entity != null) {
            val previewCenterX = previewAreaX + previewAreaW / 2
            val previewCenterY = previewAreaY + previewAreaH - 20

            try {
                InventoryScreen.renderEntityInInventory(
                    guiGraphics,
                    previewCenterX.toFloat(),
                    previewCenterY.toFloat(),
                    45f,
                    Vector3f(0f, 0f, 0f),
                    Quaternionf().rotateY(Math.toRadians(210.0).toFloat()),
                    null,
                    entity
                )
            } catch (_: Exception) {
                // Fallback text
                guiGraphics.drawCenteredString(font, "?", previewCenterX, previewAreaY + 30, GuiTheme.TEXT_MUTED)
            }
        }

        // ── Info area ──
        var infoY = previewAreaY + previewAreaH + 6

        // Pet name (large, grade color)
        val name = Component.translatable(pet.displayKey)
        guiGraphics.drawString(font, name, detailX + 2, infoY, pet.grade.color)
        infoY += 14

        // Grade
        val gradeLabel = Component.translatable("gui.estherserver.pet_storage.grade")
        val gradeValue = Component.translatable(pet.grade.translationKey)
        guiGraphics.drawString(font, gradeLabel, detailX + 2, infoY, GuiTheme.TEXT_MUTED)
        guiGraphics.drawString(font, gradeValue, detailX + 2 + font.width(gradeLabel) + 4, infoY, pet.grade.color)
        infoY += 12

        // Speed
        val speedLabel = Component.translatable("gui.estherserver.pet_storage.speed")
        val speedValue = String.format("%.2f", pet.grade.speed)
        guiGraphics.drawString(font, speedLabel, detailX + 2, infoY, GuiTheme.TEXT_MUTED)
        guiGraphics.drawString(font, speedValue, detailX + 2 + font.width(speedLabel) + 4, infoY, GuiTheme.TEXT_BODY)
        infoY += 12

        // Status
        val statusLabel = Component.translatable("gui.estherserver.pet_storage.status")
        val statusValue = if (isSummoned) {
            Component.translatable("gui.estherserver.pet_storage.summoned")
        } else {
            Component.translatable("gui.estherserver.pet_storage.standby")
        }
        val statusColor = if (isSummoned) GuiTheme.TEXT_GOLD else GuiTheme.TEXT_BODY
        guiGraphics.drawString(font, statusLabel, detailX + 2, infoY, GuiTheme.TEXT_MUTED)
        guiGraphics.drawString(font, statusValue, detailX + 2 + font.width(statusLabel) + 4, infoY, statusColor)
        infoY += 16

        // ── Action button ──
        val btnX = detailX + (panelWidth - BTN_WIDTH) / 2
        val btnY = infoY
        val btnHovered = mouseX >= btnX && mouseX < btnX + BTN_WIDTH &&
                mouseY >= btnY && mouseY < btnY + BTN_HEIGHT

        val btnBg = if (btnHovered) GuiTheme.BUTTON_HOVER else GuiTheme.BUTTON
        guiGraphics.fill(btnX, btnY, btnX + BTN_WIDTH, btnY + BTN_HEIGHT, btnBg)
        // Button border
        guiGraphics.fill(btnX, btnY, btnX + BTN_WIDTH, btnY + 1, GuiTheme.PANEL_BORDER_LIGHT)
        guiGraphics.fill(btnX, btnY + BTN_HEIGHT - 1, btnX + BTN_WIDTH, btnY + BTN_HEIGHT, GuiTheme.PANEL_BORDER_DARK)

        val btnText = if (isSummoned) {
            Component.translatable("gui.estherserver.pet_storage.dismiss")
        } else {
            Component.translatable("gui.estherserver.pet_storage.summon")
        }
        guiGraphics.drawCenteredString(font, btnText, btnX + BTN_WIDTH / 2, btnY + 4, GuiTheme.TEXT_WHITE)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val pets = PetClientHandler.cachedOwnedPets
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        // Check grid slot clicks
        for (i in pets.indices) {
            val col = i % SLOTS_PER_ROW
            val row = i / SLOTS_PER_ROW
            val slotX = guiLeft + GRID_LEFT + col * (SLOT_SIZE + SLOT_GAP)
            val slotY = guiTop + GRID_TOP + row * (SLOT_SIZE + SLOT_GAP)

            if (mx >= slotX && mx < slotX + SLOT_SIZE &&
                my >= slotY && my < slotY + SLOT_SIZE
            ) {
                selectedIndex = i
                updatePreviewEntity()
                return true
            }
        }

        // Check action button click
        if (selectedIndex in pets.indices) {
            val panelWidth = GUI_WIDTH - DETAIL_LEFT - 10
            val btnX = guiLeft + DETAIL_LEFT + (panelWidth - BTN_WIDTH) / 2
            val btnY = guiTop + DETAIL_TOP + 80 + 6 + 14 + 12 + 12 + 16 // matches infoY after status
            if (mx >= btnX && mx < btnX + BTN_WIDTH &&
                my >= btnY && my < btnY + BTN_HEIGHT
            ) {
                PacketDistributor.sendToServer(SummonPetPayload(pets[selectedIndex].name))
                onClose()
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ModKeyBindings.PET_KEY.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen(): Boolean = false
}
