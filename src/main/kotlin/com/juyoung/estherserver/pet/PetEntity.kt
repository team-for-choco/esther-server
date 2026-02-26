package com.juyoung.estherserver.pet

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.PlayerRideableJumping
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

class PetEntity(entityType: EntityType<PetEntity>, level: Level) :
    PathfinderMob(entityType, level), PlayerRideableJumping {

    var petType: PetType
        get() = PetType.fromName(entityData.get(DATA_PET_TYPE)) ?: PetType.CAT_COMMON
        set(value) { entityData.set(DATA_PET_TYPE, value.name) }

    private var jumpScale = 0f

    init {
        isInvulnerable = true
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(DATA_PET_TYPE, PetType.CAT_COMMON.name)
    }

    override fun registerGoals() {
        // No AI goals — player controls movement
    }

    // ── Riding ──

    override fun getControllingPassenger(): LivingEntity? {
        return firstPassenger as? Player
    }

    override fun getRiddenInput(player: Player, travelVector: Vec3): Vec3 {
        val forward = player.zza
        val strafe = player.xxa
        return Vec3(strafe.toDouble(), 0.0, forward.toDouble())
    }

    override fun getRiddenSpeed(player: Player): Float {
        return petType.grade.speed
    }

    // ── Jump (PlayerRideableJumping) ──

    override fun onPlayerJump(jumpPower: Int) {
        if (onGround()) {
            // jumpPower: 0~100 → jumpScale: 0.42 ~ 1.0
            jumpScale = if (jumpPower >= 90) {
                1.0f
            } else {
                0.42f + 0.58f * (jumpPower / 90f)
            }
        }
    }

    override fun canJump(): Boolean = onGround()

    override fun handleStartJump(jumpPower: Int) {}

    override fun handleStopJump() {}

    // ── Passenger ──

    override fun removePassenger(passenger: Entity) {
        super.removePassenger(passenger)
        if (!level().isClientSide && passenger is Player) {
            val data = passenger.getData(ModPets.PET_DATA.get())
            data.summonedPet = null
            data.summonedEntityId = -1
            discard()
        }
    }

    // ── Invulnerability ──

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean = false
    override fun isPushable(): Boolean = false
    @Deprecated("", level = DeprecationLevel.WARNING)
    override fun isPushedByFluid(): Boolean = false
    override fun shouldBeSaved(): Boolean = false
    override fun saveAsPassenger(tag: CompoundTag): Boolean = false
    override fun removeWhenFarAway(distanceToClosestPlayer: Double): Boolean = false
    override fun canBeLeashed(): Boolean = false

    override fun addAdditionalSaveData(compound: CompoundTag) {}
    override fun readAdditionalSaveData(compound: CompoundTag) {}

    override fun tick() {
        super.tick()
        // Sync rotation with rider + prevent fall damage
        val rider = controllingPassenger
        if (rider != null) {
            yRot = rider.yRot
            xRot = rider.xRot * 0.5f
            yBodyRot = yRot
            yHeadRot = yRot
            rider.fallDistance = 0f
        }
        fallDistance = 0f
        // Process pending jump
        if (jumpScale > 0f && onGround()) {
            deltaMovement = Vec3(deltaMovement.x, jumpScale.toDouble(), deltaMovement.z)
            jumpScale = 0f
        }
        if (!level().isClientSide && passengers.isEmpty()) {
            discard()
        }
    }

    companion object {
        private val DATA_PET_TYPE: EntityDataAccessor<String> =
            SynchedEntityData.defineId(PetEntity::class.java, EntityDataSerializers.STRING)

        fun createAttributes(): AttributeSupplier.Builder = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.14)
    }
}
