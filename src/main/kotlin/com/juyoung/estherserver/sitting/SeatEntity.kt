package com.juyoung.estherserver.sitting

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

class SeatEntity(entityType: EntityType<SeatEntity>, level: Level) : Entity(entityType, level) {

    init {
        noPhysics = true
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        // 커스텀 동기화 데이터 불필요 (기본 데이터는 Entity 생성자에서 등록됨)
    }

    override fun tick() {
        super.tick()
        if (!level().isClientSide && passengers.isEmpty()) {
            discard()
        }
    }

    override fun shouldBeSaved(): Boolean = false

    override fun readAdditionalSaveData(compound: CompoundTag) {}

    override fun addAdditionalSaveData(compound: CompoundTag) {}

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean {
        return false
    }

    override fun removePassenger(passenger: Entity) {
        super.removePassenger(passenger)
        discard()
    }
}
