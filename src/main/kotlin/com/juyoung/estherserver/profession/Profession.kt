package com.juyoung.estherserver.profession

enum class Profession(val translationKey: String) {
    FISHING("profession.estherserver.fishing"),
    FARMING("profession.estherserver.farming"),
    MINING("profession.estherserver.mining"),
    COOKING("profession.estherserver.cooking");

    companion object {
        const val MAX_LEVEL = 50

        private val XP_TABLE = listOf(
            10 to 50,
            20 to 100,
            30 to 200,
            40 to 600,
            MAX_LEVEL to 3000
        )

        fun getRequiredXp(level: Int): Int {
            if (level <= 0) return 0
            if (level > MAX_LEVEL) return Int.MAX_VALUE
            return XP_TABLE.first { level <= it.first }.second
        }
    }
}
