package com.juyoung.estherserver.pet

/**
 * 펫 종류 정의.
 */
enum class PetType(
    val displayKey: String,
    val grade: PetGrade,
    val textureId: String
) {
    CAT_COMMON("pet.estherserver.cat_common", PetGrade.COMMON, "cat_common"),
    DOG_COMMON("pet.estherserver.dog_common", PetGrade.COMMON, "dog_common"),
    RABBIT_COMMON("pet.estherserver.rabbit_common", PetGrade.COMMON, "rabbit_common"),
    FOX_COMMON("pet.estherserver.fox_common", PetGrade.COMMON, "fox_common");

    companion object {
        fun fromName(name: String): PetType? = try {
            valueOf(name)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
