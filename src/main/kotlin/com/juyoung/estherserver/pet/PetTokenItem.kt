package com.juyoung.estherserver.pet

import com.juyoung.estherserver.economy.EconomyHandler
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class PetTokenItem(
    private val petType: PetType,
    properties: Properties
) : Item(properties) {

    private val duplicateCurrency = mapOf(
        PetGrade.COMMON to 500L,
        PetGrade.FINE to 1500L,
        PetGrade.RARE to 5000L
    )

    override fun use(
        level: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        val stack = player.getItemInHand(usedHand)
        val petData = serverPlayer.getData(ModPets.PET_DATA.get())
        val displayName = Component.translatable(petType.displayKey)

        if (petType in petData.ownedPets) {
            val compensation = duplicateCurrency[petType.grade] ?: 500L
            EconomyHandler.addBalance(serverPlayer, compensation)
            serverPlayer.sendSystemMessage(
                Component.translatable(
                    "message.estherserver.pet_token_duplicate",
                    displayName,
                    compensation
                )
            )
        } else {
            petData.ownedPets.add(petType)
            serverPlayer.setData(ModPets.PET_DATA.get(), petData)
            serverPlayer.sendSystemMessage(
                Component.translatable("message.estherserver.pet_token_registered", displayName)
            )
        }

        level.playSound(
            null,
            serverPlayer.blockPosition(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.8f, 1.2f
        )

        stack.shrink(1)
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("tooltip.estherserver.pet_token", Component.translatable(petType.displayKey))
        )
    }
}
