package com.juyoung.estherserver.quest

import com.juyoung.estherserver.profession.ProfessionBonusHelper.ContentGrade

enum class QuestTrackingType {
    CATCH_FISH,
    HARVEST_CROP,
    MINE_ORE,
    COOK_DISH,
    SELL_ITEMS,
    EARN_CURRENCY,
    REGISTER_COLLECTION
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
    val gradeFilter: ContentGrade? = null,
    val baseCurrencyReward: Int,
    val isWeekly: Boolean = false
) {
    val translationKey: String get() = "quest.estherserver.$id"
}
