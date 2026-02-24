package com.juyoung.estherserver.wild

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent
import net.neoforged.neoforge.event.level.ExplosionEvent

object OverworldProtectionHandler {

    private fun isOverworld(level: Level): Boolean =
        level.dimension() == Level.OVERWORLD

    // A. 적대적 몹 스폰 차단
    @SubscribeEvent
    fun onEntityJoinLevel(event: EntityJoinLevelEvent) {
        val level = event.level
        if (level.isClientSide) return
        if (!isOverworld(level)) return

        if (event.entity.type.category == MobCategory.MONSTER) {
            event.isCanceled = true
        }
    }

    // C. PvP 비활성화 — 직접 공격
    @SubscribeEvent
    fun onAttackEntity(event: AttackEntityEvent) {
        val attacker = event.entity
        val level = attacker.level()
        if (level.isClientSide) return
        if (!isOverworld(level)) return

        if (attacker is Player && event.target is Player) {
            event.isCanceled = true
            (attacker as? ServerPlayer)?.displayClientMessage(
                Component.translatable("message.estherserver.overworld_pvp_blocked"), true
            )
        }
    }

    // C. PvP 비활성화 — 투사체 등 간접 피해
    @SubscribeEvent
    fun onLivingIncomingDamage(event: LivingIncomingDamageEvent) {
        val target = event.entity
        val level = target.level()
        if (level.isClientSide) return
        if (!isOverworld(level)) return

        if (target is Player) {
            val sourceEntity = event.source.entity
            if (sourceEntity is Player && sourceEntity != target) {
                event.isCanceled = true
            }
        }
    }

    // D. 폭발 피해 차단
    @SubscribeEvent
    fun onExplosionDetonate(event: ExplosionEvent.Detonate) {
        val level = event.level
        if (!isOverworld(level)) return

        event.affectedBlocks.clear()
        event.affectedEntities.clear()
    }
}
