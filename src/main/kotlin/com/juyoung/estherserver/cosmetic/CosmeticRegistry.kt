package com.juyoung.estherserver.cosmetic

import net.minecraft.world.entity.EquipmentSlot

/**
 * 치장 아이템 정의.
 * @param id 치장 고유 ID (e.g., "cat_ears")
 * @param slot 장착 부위
 * @param setId 아머 텍스처 세트 ID (equipment JSON 이름, e.g., "cosmetic_cat")
 * @param displayKey 번역 키
 * @param grade 등급 (Common, Fine, Rare 등)
 */
data class CosmeticDef(
    val id: String,
    val slot: EquipmentSlot,
    val setId: String,
    val displayKey: String,
    val grade: CosmeticGrade = CosmeticGrade.COMMON
) {
    /** 토큰 아이템 레지스트리 이름 (e.g., "cosmetic_token_cat_ears") */
    val tokenItemId: String get() = "cosmetic_token_$id"
}

enum class CosmeticGrade(val translationKey: String, val color: Int) {
    COMMON("cosmetic.estherserver.grade.common", 0xFFFFFFFF.toInt()),
    FINE("cosmetic.estherserver.grade.fine", 0xFF55FF55.toInt()),
    RARE("cosmetic.estherserver.grade.rare", 0xFF5BC8F5.toInt()),
    HEROIC("cosmetic.estherserver.grade.heroic", 0xFFAA00FF.toInt()),
    LEGENDARY("cosmetic.estherserver.grade.legendary", 0xFFFFAA00.toInt())
}

object CosmeticRegistry {
    private val cosmetics = mutableMapOf<String, CosmeticDef>()

    fun init() {
        // ── 고양이 세트 (Common) ──
        register(CosmeticDef("cat_ears", EquipmentSlot.HEAD, "cosmetic_cat", "cosmetic.estherserver.cat_ears"))
        register(CosmeticDef("cat_hoodie", EquipmentSlot.CHEST, "cosmetic_cat", "cosmetic.estherserver.cat_hoodie"))
        register(CosmeticDef("cat_pants", EquipmentSlot.LEGS, "cosmetic_cat", "cosmetic.estherserver.cat_pants"))
        register(CosmeticDef("cat_paws", EquipmentSlot.FEET, "cosmetic_cat", "cosmetic.estherserver.cat_paws"))

        // ── 강아지 세트 (Common) ──
        register(CosmeticDef("dog_ears", EquipmentSlot.HEAD, "cosmetic_dog", "cosmetic.estherserver.dog_ears"))
        register(CosmeticDef("dog_hoodie", EquipmentSlot.CHEST, "cosmetic_dog", "cosmetic.estherserver.dog_hoodie"))
        register(CosmeticDef("dog_pants", EquipmentSlot.LEGS, "cosmetic_dog", "cosmetic.estherserver.dog_pants"))
        register(CosmeticDef("dog_paws", EquipmentSlot.FEET, "cosmetic_dog", "cosmetic.estherserver.dog_paws"))

        // ── 토끼 세트 (Common) ──
        register(CosmeticDef("rabbit_ears", EquipmentSlot.HEAD, "cosmetic_rabbit", "cosmetic.estherserver.rabbit_ears"))
        register(CosmeticDef("rabbit_hoodie", EquipmentSlot.CHEST, "cosmetic_rabbit", "cosmetic.estherserver.rabbit_hoodie"))
        register(CosmeticDef("rabbit_pants", EquipmentSlot.LEGS, "cosmetic_rabbit", "cosmetic.estherserver.rabbit_pants"))
        register(CosmeticDef("rabbit_paws", EquipmentSlot.FEET, "cosmetic_rabbit", "cosmetic.estherserver.rabbit_paws"))

        // ── 여우 세트 (Common) ──
        register(CosmeticDef("fox_ears", EquipmentSlot.HEAD, "cosmetic_fox", "cosmetic.estherserver.fox_ears"))
        register(CosmeticDef("fox_hoodie", EquipmentSlot.CHEST, "cosmetic_fox", "cosmetic.estherserver.fox_hoodie"))
        register(CosmeticDef("fox_pants", EquipmentSlot.LEGS, "cosmetic_fox", "cosmetic.estherserver.fox_pants"))
        register(CosmeticDef("fox_paws", EquipmentSlot.FEET, "cosmetic_fox", "cosmetic.estherserver.fox_paws"))
    }

    private fun register(def: CosmeticDef) {
        cosmetics[def.id] = def
    }

    fun get(id: String): CosmeticDef? = cosmetics[id]

    fun getAll(): Collection<CosmeticDef> = cosmetics.values

    fun getBySlot(slot: EquipmentSlot): List<CosmeticDef> =
        cosmetics.values.filter { it.slot == slot }

    fun getAllIds(): Set<String> = cosmetics.keys.toSet()
}
