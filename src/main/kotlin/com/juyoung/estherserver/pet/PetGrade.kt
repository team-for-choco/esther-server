package com.juyoung.estherserver.pet

/**
 * 펫 등급 — 등급별 이동 속도 차등.
 */
enum class PetGrade(
    val translationKey: String,
    val speed: Float,
    val color: Int
) {
    COMMON("pet.estherserver.grade.common", 0.14f, 0xFFFFFFFF.toInt()),
    FINE("pet.estherserver.grade.fine", 0.18f, 0xFF55FF55.toInt()),
    RARE("pet.estherserver.grade.rare", 0.22f, 0xFF5BC8F5.toInt());
}
