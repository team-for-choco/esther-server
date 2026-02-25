package com.juyoung.estherserver.quest

import com.juyoung.estherserver.profession.ProfessionBonusHelper.ContentGrade

object QuestPool {

    private val dailyPool = mutableListOf<QuestTemplate>()
    private val weeklyPool = mutableListOf<QuestTemplate>()

    init {
        registerDailyQuests()
        registerWeeklyQuests()
    }

    private fun registerDailyQuests() {
        // Fishing (FISHING category)
        dailyPool.add(QuestTemplate("daily_catch_fish_3", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 3, null, 15))
        dailyPool.add(QuestTemplate("daily_catch_fish_5", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 5, null, 15))
        dailyPool.add(QuestTemplate("daily_catch_advanced_fish_2", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 2, ContentGrade.ADVANCED, 23))
        dailyPool.add(QuestTemplate("daily_catch_rare_fish_1", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 1, ContentGrade.RARE, 30))

        // Farming (FARMING category)
        dailyPool.add(QuestTemplate("daily_harvest_crop_3", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 3, null, 15))
        dailyPool.add(QuestTemplate("daily_harvest_crop_5", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 5, null, 15))
        dailyPool.add(QuestTemplate("daily_harvest_advanced_crop_2", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 2, ContentGrade.ADVANCED, 23))
        dailyPool.add(QuestTemplate("daily_harvest_rare_crop_1", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 1, ContentGrade.RARE, 30))

        // Mining (MINING category)
        dailyPool.add(QuestTemplate("daily_mine_ore_3", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 3, null, 15))
        dailyPool.add(QuestTemplate("daily_mine_ore_5", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 5, null, 15))
        dailyPool.add(QuestTemplate("daily_mine_advanced_ore_2", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 2, ContentGrade.ADVANCED, 23))
        dailyPool.add(QuestTemplate("daily_mine_rare_ore_1", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 1, ContentGrade.RARE, 30))

        // Cooking (COOKING category)
        dailyPool.add(QuestTemplate("daily_cook_dish_2", QuestTrackingType.COOK_DISH, QuestCategory.COOKING, 2, null, 20))
        dailyPool.add(QuestTemplate("daily_cook_dish_3", QuestTrackingType.COOK_DISH, QuestCategory.COOKING, 3, null, 20))

        // General (sell, earn, collect)
        dailyPool.add(QuestTemplate("daily_sell_items_5", QuestTrackingType.SELL_ITEMS, QuestCategory.GENERAL, 5, null, 10))
        dailyPool.add(QuestTemplate("daily_sell_items_10", QuestTrackingType.SELL_ITEMS, QuestCategory.GENERAL, 10, null, 10))
        dailyPool.add(QuestTemplate("daily_earn_currency_50", QuestTrackingType.EARN_CURRENCY, QuestCategory.GENERAL, 50, null, 10))
        dailyPool.add(QuestTemplate("daily_earn_currency_100", QuestTrackingType.EARN_CURRENCY, QuestCategory.GENERAL, 100, null, 10))
        dailyPool.add(QuestTemplate("daily_register_collection_1", QuestTrackingType.REGISTER_COLLECTION, QuestCategory.GENERAL, 1, null, 25))
        dailyPool.add(QuestTemplate("daily_register_collection_2", QuestTrackingType.REGISTER_COLLECTION, QuestCategory.GENERAL, 2, null, 25))
    }

    private fun registerWeeklyQuests() {
        // Fishing
        weeklyPool.add(QuestTemplate("weekly_catch_fish_15", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 15, null, 45, true))
        weeklyPool.add(QuestTemplate("weekly_catch_advanced_fish_5", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 5, ContentGrade.ADVANCED, 68, true))
        weeklyPool.add(QuestTemplate("weekly_catch_rare_fish_3", QuestTrackingType.CATCH_FISH, QuestCategory.FISHING, 3, ContentGrade.RARE, 90, true))

        // Farming
        weeklyPool.add(QuestTemplate("weekly_harvest_crop_15", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 15, null, 45, true))
        weeklyPool.add(QuestTemplate("weekly_harvest_advanced_crop_5", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 5, ContentGrade.ADVANCED, 68, true))
        weeklyPool.add(QuestTemplate("weekly_harvest_rare_crop_3", QuestTrackingType.HARVEST_CROP, QuestCategory.FARMING, 3, ContentGrade.RARE, 90, true))

        // Mining
        weeklyPool.add(QuestTemplate("weekly_mine_ore_15", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 15, null, 45, true))
        weeklyPool.add(QuestTemplate("weekly_mine_advanced_ore_5", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 5, ContentGrade.ADVANCED, 68, true))
        weeklyPool.add(QuestTemplate("weekly_mine_rare_ore_3", QuestTrackingType.MINE_ORE, QuestCategory.MINING, 3, ContentGrade.RARE, 90, true))

        // Cooking
        weeklyPool.add(QuestTemplate("weekly_cook_dish_8", QuestTrackingType.COOK_DISH, QuestCategory.COOKING, 8, null, 60, true))

        // General
        weeklyPool.add(QuestTemplate("weekly_sell_items_25", QuestTrackingType.SELL_ITEMS, QuestCategory.GENERAL, 25, null, 30, true))
        weeklyPool.add(QuestTemplate("weekly_earn_currency_300", QuestTrackingType.EARN_CURRENCY, QuestCategory.GENERAL, 300, null, 30, true))
        weeklyPool.add(QuestTemplate("weekly_register_collection_5", QuestTrackingType.REGISTER_COLLECTION, QuestCategory.GENERAL, 5, null, 75, true))
    }

    /**
     * Select 5 quests with balanced category distribution:
     * 1 from each of the 4 profession categories + 1 from remaining pool.
     * Uses day-based seed for deterministic daily/weekly selection across all players.
     */
    fun selectQuests(seed: Long, weekly: Boolean): List<QuestTemplate> {
        val pool = if (weekly) weeklyPool else dailyPool
        val random = java.util.Random(seed)
        val selected = mutableListOf<QuestTemplate>()

        // Group by category
        val byCategory = pool.groupBy { it.category }

        // Pick 1 from each of the 4 profession categories
        val professionCategories = listOf(QuestCategory.FISHING, QuestCategory.FARMING, QuestCategory.MINING, QuestCategory.COOKING)
        for (category in professionCategories) {
            val candidates = byCategory[category] ?: continue
            if (candidates.isNotEmpty()) {
                selected.add(candidates[random.nextInt(candidates.size)])
            }
        }

        // Pick 1 more from GENERAL or any remaining
        val generalCandidates = byCategory[QuestCategory.GENERAL] ?: emptyList()
        if (generalCandidates.isNotEmpty()) {
            selected.add(generalCandidates[random.nextInt(generalCandidates.size)])
        }

        // Ensure we have exactly 5 (fill from remaining pool if needed)
        if (selected.size < 5) {
            val remaining = pool.filter { it !in selected }
            val shuffled = remaining.toMutableList()
            shuffled.shuffle(random)
            while (selected.size < 5 && shuffled.isNotEmpty()) {
                selected.add(shuffled.removeFirst())
            }
        }

        return selected.take(5)
    }

    fun getTemplate(id: String): QuestTemplate? {
        return dailyPool.find { it.id == id } ?: weeklyPool.find { it.id == id }
    }
}
