package com.juyoung.estherserver.quest

enum class QuestTrackingType {
    SUBMIT_ITEM,
    KILL_MONSTER
}

enum class QuestCategory {
    FISHING,
    FARMING,
    MINING,
    COOKING,
    GENERAL
}

data class QuestTemplate(
    val id: String,
    val trackingType: QuestTrackingType,
    val category: QuestCategory,
    val targetCount: Int,
    val targetItemId: String? = null,
    val targetEntityTypes: List<String>? = null,
    val currencyReward: Int,
    val huntersPotReward: Int,
    val isWeekly: Boolean = false
) {
    val translationKey: String get() = "quest.estherserver.$id"
}
