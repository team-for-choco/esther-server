package com.juyoung.estherserver.merchant

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

class MerchantEntity(entityType: EntityType<MerchantEntity>, level: Level) :
    PathfinderMob(entityType, level) {

    var merchantType: ShopCategory = ShopCategory.SEEDS

    init {
        isInvulnerable = true
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, LookAtPlayerGoal(this, Player::class.java, 8.0f))
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean {
        return false
    }

    override fun isPushable(): Boolean = false

    @Suppress("DEPRECATION")
    override fun isPushedByFluid(): Boolean = false

    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean = false

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS
        if (player is ServerPlayer) {
            PacketDistributor.sendToPlayer(player, OpenShopPayload(this.id, merchantType.name))
        }
        return InteractionResult.SUCCESS
    }

    override fun canBeLeashed(): Boolean = false

    override fun isPersistenceRequired(): Boolean = true

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.putString("MerchantType", merchantType.name)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains("MerchantType")) {
            merchantType = try {
                ShopCategory.valueOf(compound.getString("MerchantType"))
            } catch (_: IllegalArgumentException) {
                ShopCategory.SEEDS
            }
        }
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
    }
}
