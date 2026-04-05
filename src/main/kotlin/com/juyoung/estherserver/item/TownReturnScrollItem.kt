package com.juyoung.estherserver.item

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.world.entity.Relative
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

/**
 * 마을 귀환서.
 * 우클릭으로 즉시 사용 → 3초 카운트다운(서버 틱 기반) → 거점 스폰지점 텔레포트.
 * 카운트다운 중 액션바에 3 / 2 / 1 표시.
 */
class TownReturnScrollItem(properties: Properties) : Item(properties) {

    companion object {
        /** 카운트다운 시간 (틱). 3초 = 60틱 */
        private const val COUNTDOWN_TICKS = 60

        /** 플레이어별 카운트다운 데이터 */
        private val countdowns = ConcurrentHashMap<UUID, CountdownData>()

        data class CountdownData(
            val startTick: Int,
            val hand: InteractionHand
        )

        /** 서버 틱 이벤트 핸들러 — EstherServerMod에서 등록 */
        @SubscribeEvent
        fun onServerTick(event: ServerTickEvent.Post) {
            val iterator = countdowns.entries.iterator()
            while (iterator.hasNext()) {
                val (uuid, data) = iterator.next()
                val server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer() ?: continue
                val player = server.playerList.getPlayer(uuid)
                if (player == null) {
                    iterator.remove()
                    continue
                }

                val elapsed = server.tickCount - data.startTick
                val remaining = COUNTDOWN_TICKS - elapsed

                if (remaining <= 0) {
                    // 카운트다운 완료 → 텔레포트
                    iterator.remove()
                    executeTeleport(player, data.hand)
                    continue
                }

                // 카운트다운 표시 (초 단위 전환 시)
                val currentSecond = (remaining + 19) / 20 // 올림: 60~41→3, 40~21→2, 20~1→1
                val prevRemaining = remaining + 1
                val prevSecond = (prevRemaining + 19) / 20

                if (elapsed <= 1 || currentSecond != prevSecond) {
                    player.sendSystemMessage(
                        Component.translatable("message.estherserver.town_return_countdown", currentSecond),
                        true
                    )
                    player.level().playSound(
                        null, player.blockPosition(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS,
                        0.5f, 1.0f
                    )
                }
            }
        }

        private fun executeTeleport(player: ServerPlayer, hand: InteractionHand) {
            val server = player.server
            val overworld = server.getLevel(Level.OVERWORLD)
            if (overworld == null) {
                player.sendSystemMessage(
                    Component.translatable("message.estherserver.town_return_failed")
                )
                return
            }

            // 아이템 소모를 텔레포트 전에 처리 (차원 이동 후 플레이어 객체 교체 시 참조 무효화 방지)
            if (!player.abilities.instabuild) {
                val stack = player.getItemInHand(hand)
                if (!stack.isEmpty && stack.item is TownReturnScrollItem) {
                    stack.shrink(1)
                }
            }

            val spawnPos = overworld.sharedSpawnPos

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

            overworld.playSound(
                null, spawnPos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                1.0f, 1.0f
            )

            player.sendSystemMessage(
                Component.translatable("message.estherserver.town_return_success")
            )
        }

        fun isCountingDown(uuid: UUID): Boolean = countdowns.containsKey(uuid)

        fun cancelCountdown(uuid: UUID) {
            countdowns.remove(uuid)
        }
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS

        // 이미 카운트다운 중이면 무시
        if (isCountingDown(player.uuid)) return InteractionResult.PASS

        // 카운트다운 시작
        countdowns[player.uuid] = CountdownData(
            startTick = serverPlayer.server.tickCount,
            hand = usedHand
        )

        return InteractionResult.SUCCESS
    }
}
