package com.juyoung.estherserver.pet

import com.juyoung.estherserver.gui.GuiTheme
import com.juyoung.estherserver.sitting.ModKeyBindings
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn
import net.neoforged.neoforge.network.PacketDistributor

@OnlyIn(Dist.CLIENT)
class PetStorageScreen : Screen(Component.translatable("gui.estherserver.pet_storage.title")) {

    private var guiLeft = 0
    private var guiTop = 0

    companion object {
        private const val GUI_WIDTH = 200
        private const val GUI_HEIGHT = 160
        private const val SLOT_SIZE = 40
        private const val SLOT_GAP = 6
        private const val SLOTS_PER_ROW = 4
        private const val GRID_LEFT = 12
        private const val GRID_TOP = 24
    }

    override fun init() {
        super.init()
        guiLeft = (width - GUI_WIDTH) / 2
        guiTop = (height - GUI_HEIGHT) / 2
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        // Panel background
        GuiTheme.renderPanel(guiGraphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT)

        // Title
        guiGraphics.drawCenteredString(font, title, guiLeft + GUI_WIDTH / 2, guiTop + 8, GuiTheme.TEXT_TITLE)

        val pets = PetClientHandler.cachedOwnedPets
        val summonedPet = PetClientHandler.cachedSummonedPet
        val maxSlots = 8

        for (i in 0 until maxSlots) {
            val col = i % SLOTS_PER_ROW
            val row = i / SLOTS_PER_ROW
            val slotX = guiLeft + GRID_LEFT + col * (SLOT_SIZE + SLOT_GAP)
            val slotY = guiTop + GRID_TOP + row * (SLOT_SIZE + SLOT_GAP)

            val isHovered = mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                    mouseY >= slotY && mouseY < slotY + SLOT_SIZE

            if (i < pets.size) {
                val pet = pets[i]
                val isSummoned = pet == summonedPet

                // Slot background
                val bg = when {
                    isSummoned -> GuiTheme.ROW_SELECTED
                    isHovered -> GuiTheme.CELL_HOVER
                    else -> GuiTheme.CELL_BG
                }
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, bg)

                // Grade border
                val borderColor = pet.grade.color
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + 1, borderColor)
                guiGraphics.fill(slotX, slotY + SLOT_SIZE - 1, slotX + SLOT_SIZE, slotY + SLOT_SIZE, borderColor)
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + SLOT_SIZE, borderColor)
                guiGraphics.fill(slotX + SLOT_SIZE - 1, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, borderColor)

                // Pet name
                val name = Component.translatable(pet.displayKey)
                guiGraphics.drawCenteredString(font, name, slotX + SLOT_SIZE / 2, slotY + 6, GuiTheme.TEXT_WHITE)

                // Grade name
                val grade = Component.translatable(pet.grade.translationKey)
                guiGraphics.drawCenteredString(font, grade, slotX + SLOT_SIZE / 2, slotY + 18, pet.grade.color)

                // Summoned indicator
                if (isSummoned) {
                    val summonText = Component.translatable("gui.estherserver.pet_storage.summoned")
                    guiGraphics.drawCenteredString(font, summonText, slotX + SLOT_SIZE / 2, slotY + 30, GuiTheme.TEXT_GOLD)
                }
            } else {
                // Empty slot
                guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, GuiTheme.SLOT_LOCKED)
            }
        }

        // Bottom hint
        val hint = Component.translatable("gui.estherserver.pet_storage.hint")
        guiGraphics.drawCenteredString(font, hint, guiLeft + GUI_WIDTH / 2, guiTop + GUI_HEIGHT - 16, GuiTheme.TEXT_MUTED)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val pets = PetClientHandler.cachedOwnedPets

        for (i in pets.indices) {
            val col = i % SLOTS_PER_ROW
            val row = i / SLOTS_PER_ROW
            val slotX = guiLeft + GRID_LEFT + col * (SLOT_SIZE + SLOT_GAP)
            val slotY = guiTop + GRID_TOP + row * (SLOT_SIZE + SLOT_GAP)

            if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE &&
                mouseY >= slotY && mouseY < slotY + SLOT_SIZE
            ) {
                PacketDistributor.sendToServer(SummonPetPayload(pets[i].name))
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
