package com.juyoung.estherserver.pet

/**
 * 펫 종류 정의.
 */
enum class PetType(
    val displayKey: String,
    val grade: PetGrade,
    val textureId: String
) {
    CAT_COMMON("pet.estherserver.cat_common", PetGrade.COMMON, "cat_common");

    companion object {
        fun fromName(name: String): PetType? = try {
            valueOf(name)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
