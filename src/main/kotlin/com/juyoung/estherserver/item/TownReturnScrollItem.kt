package com.juyoung.estherserver.item

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.level.Level
import java.util.EnumSet
import net.minecraft.world.entity.Relative

/**
 * 마을 귀환서.
 * 우클릭으로 사용 시 3초 카운트다운 후 거점(오버월드) 스폰지점으로 텔레포트.
 * 카운트다운 중 화면에 3 / 2 / 1 표시.
 */
class TownReturnScrollItem(properties: Properties) : Item(properties) {

    companion object {
        /** 사용 시간 (틱). 3초 = 60틱 */
        private const val USE_DURATION = 60
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int = USE_DURATION

    override fun getUseAnimation(stack: ItemStack): ItemUseAnimation = ItemUseAnimation.BOW

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResult {
        player.startUsingItem(usedHand)
        return InteractionResult.CONSUME
    }

    override fun onUseTick(level: Level, livingEntity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        if (level.isClientSide) return
        val player = livingEntity as? ServerPlayer ?: return

        // 남은 틱 → 카운트다운 숫자
        // 60~41틱 = "3", 40~21틱 = "2", 20~1틱 = "1"
        val countdown = when {
            remainingUseDuration > 40 -> 3
            remainingUseDuration > 20 -> 2
            else -> 1
        }

        // 각 카운트 전환 시점에만 Title 전송 (매 틱 전송 방지)
        if (remainingUseDuration == USE_DURATION - 1 || remainingUseDuration == 40 || remainingUseDuration == 20) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.town_return_countdown", countdown),
                true // actionbar
            )

            // 카운트다운 효과음
            level.playSound(
                null, player.blockPosition(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS,
                0.5f, 1.0f
            )
        }
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, livingEntity: LivingEntity): ItemStack {
        if (level.isClientSide) return stack
        val player = livingEntity as? ServerPlayer ?: return stack

        val server = player.server
        val overworld = server.getLevel(Level.OVERWORLD)
        if (overworld == null) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.town_return_failed")
            )
            return stack
        }

        val spawnPos = overworld.sharedSpawnPos

        // 텔레포트
        player.teleportTo(
            overworld,
            spawnPos.x.toDouble() + 0.5,
            spawnPos.y.toDouble(),
            spawnPos.z.toDouble() + 0.5,
            EnumSet.noneOf(Relative::class.java),
            player.yRot,
            player.xRot,
            false
        )

        // 효과음 (도착 차원에서 재생)
        overworld.playSound(
            null, spawnPos,
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
            1.0f, 1.0f
        )

        // 아이템 소모 (크리에이티브 제외)
        if (!player.abilities.instabuild) stack.shrink(1)

        player.sendSystemMessage(
            Component.translatable("message.estherserver.town_return_success")
        )

        return stack
    }
}
