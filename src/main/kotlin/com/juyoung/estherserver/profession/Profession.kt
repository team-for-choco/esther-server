package com.juyoung.estherserver.profession

enum class Profession(val translationKey: String) {
    FISHING("profession.estherserver.fishing"),
    FARMING("profession.estherserver.farming"),
    MINING("profession.estherserver.mining"),
    COOKING("profession.estherserver.cooking");

    companion object {
        const val MAX_LEVEL = 50

        fun getRequiredXp(level: Int): Int = when {
            level < 1 -> 0
            level <= 10 -> 50
            level <= 20 -> 100
            level <= 30 -> 200
            level <= 40 -> 600
            level <= 50 -> 3000
            else -> Int.MAX_VALUE
        }
    }
}
