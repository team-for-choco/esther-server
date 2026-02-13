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
        // 동기화 데이터 불필요
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        // 비저장 — 서버 재시작 시 자동 정리
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        // 비저장
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean {
        return false
    }

    override fun removePassenger(passenger: Entity) {
        super.removePassenger(passenger)
        discard()
    }
}
