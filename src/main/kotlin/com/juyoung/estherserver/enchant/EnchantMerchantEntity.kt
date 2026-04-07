package com.juyoung.estherserver.enchant

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.neoforge.network.PacketDistributor

class EnchantMerchantEntity(entityType: EntityType<EnchantMerchantEntity>, level: Level) :
    PathfinderMob(entityType, level) {

    init {
        isInvulnerable = true
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, LookAtPlayerGoal(this, Player::class.java, 8.0f))
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean = false

    override fun isPushable(): Boolean = false

    @Suppress("DEPRECATION")
    override fun isPushedByFluid(): Boolean = false

    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean = false

    override fun canBeLeashed(): Boolean = false

    override fun isPersistenceRequired(): Boolean = true

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS
        if (player is ServerPlayer) {
            PacketDistributor.sendToPlayer(player, OpenEnchantMerchantPayload())
        }
        return InteractionResult.SUCCESS
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
    }
}
