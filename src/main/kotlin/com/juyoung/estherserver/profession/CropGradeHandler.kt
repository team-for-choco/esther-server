package com.juyoung.estherserver.profession

import com.juyoung.estherserver.block.CustomCropBlock
import com.juyoung.estherserver.enhancement.EnhancementHandler
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.BlockEvent

object CropGradeHandler {

    @SubscribeEvent
    fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
        val block = event.placedBlock.block
        if (block !is CustomCropBlock) return

        val player = event.entity as? ServerPlayer ?: return

        val blockId = BuiltInRegistries.BLOCK.getKey(block)
        val path = blockId.path
        if (!path.endsWith("_crop")) return

        val cropId = ResourceLocation.fromNamespaceAndPath(blockId.namespace, path.removeSuffix("_crop"))
        val grade = ProfessionBonusHelper.getCropGrade(cropId) ?: return

        val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FARMING)
        if (!ProfessionBonusHelper.canHarvestCustomCrops(equipLevel.coerceAtLeast(0)) ||
            grade > ProfessionBonusHelper.getMaxCropGrade(equipLevel.coerceAtLeast(0))
        ) {
            event.isCanceled = true
            player.inventoryMenu.sendAllDataToRemote()
            player.displayClientMessage(
                Component.translatable("message.estherserver.seed_grade_locked"), true
            )
        }
    }
}
