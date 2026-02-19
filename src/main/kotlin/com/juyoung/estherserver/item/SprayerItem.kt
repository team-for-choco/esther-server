package com.juyoung.estherserver.item

import com.juyoung.estherserver.block.SpecialFarmlandBlock
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.material.Fluids

class SprayerItem(properties: Properties) : Item(properties) {
    companion object {
        const val MAX_CHARGES = 3
    }

    override fun use(
        level: net.minecraft.world.level.Level,
        player: net.minecraft.world.entity.player.Player,
        usedHand: net.minecraft.world.InteractionHand
    ): InteractionResult {
        val stack = player.getItemInHand(usedHand)
        val hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY)

        if (hitResult.type == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            val fluidState = level.getFluidState(hitResult.blockPos)
            if (fluidState.`is`(Fluids.WATER) && fluidState.isSource) {
                stack.set(ModDataComponents.WATER_CHARGES.get(), MAX_CHARGES)

                if (!level.isClientSide) {
                    level.playSound(
                        null, player.blockPosition(),
                        SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f
                    )
                    player.displayClientMessage(
                        Component.translatable(
                            "message.estherserver.sprayer_filled",
                            MAX_CHARGES, MAX_CHARGES
                        ), true
                    )
                }
                return InteractionResult.SUCCESS
            }
        }
        return InteractionResult.PASS
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos
        val stack = context.itemInHand
        val player = context.player ?: return InteractionResult.PASS
        val state = level.getBlockState(pos)

        if (state.block is SpecialFarmlandBlock) {
            if (level.isClientSide) return InteractionResult.SUCCESS

            val charges = stack.getOrDefault(ModDataComponents.WATER_CHARGES.get(), 0)
            if (charges <= 0) {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.sprayer_empty"), true
                )
                return InteractionResult.FAIL
            }

            if (state.getValue(SpecialFarmlandBlock.MOISTURE) == 1) {
                return InteractionResult.PASS
            }

            level.setBlock(pos, state.setValue(SpecialFarmlandBlock.MOISTURE, 1), 3)
            stack.set(ModDataComponents.WATER_CHARGES.get(), charges - 1)

            val serverLevel = level as ServerLevel
            serverLevel.sendParticles(
                ParticleTypes.SPLASH,
                pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                10, 0.3, 0.1, 0.3, 0.05
            )
            level.playSound(
                null, pos,
                SoundEvents.WEATHER_RAIN, SoundSource.BLOCKS, 0.5f, 1.2f
            )

            player.displayClientMessage(
                Component.translatable(
                    "message.estherserver.sprayer_used",
                    charges - 1, MAX_CHARGES
                ), true
            )
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val charges = stack.getOrDefault(ModDataComponents.WATER_CHARGES.get(), 0)
        tooltipComponents.add(
            Component.translatable("tooltip.estherserver.sprayer_charges", charges, MAX_CHARGES)
        )
    }
}
