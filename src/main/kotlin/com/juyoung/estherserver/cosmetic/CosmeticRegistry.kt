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
