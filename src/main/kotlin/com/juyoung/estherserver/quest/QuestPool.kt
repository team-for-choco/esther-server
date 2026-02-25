package com.juyoung.estherserver.quest

object QuestPool {

    private val dailyPool = mutableListOf<QuestTemplate>()
    private val weeklyPool = mutableListOf<QuestTemplate>()

    init {
        registerDailyQuests()
        registerWeeklyQuests()
    }

    private fun registerDailyQuests() {
        // Fishing — 7 templates (specific fish, 3 each)
        dailyPool.add(QuestTemplate("daily_submit_crucian_carp", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:crucian_carp", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_anchovy", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:anchovy", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_salmon_catch", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:salmon_catch", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_mackerel", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:mackerel", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_hairtail", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:hairtail", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_sea_bream", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:sea_bream", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_clam", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 3, "estherserver:clam", currencyReward = 1000, huntersPotReward = 30))

        // Farming — 8 templates (specific crop, 3 each)
        dailyPool.add(QuestTemplate("daily_submit_rice", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:rice", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_red_pepper", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:red_pepper", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_spinach", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:spinach", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_green_onion", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:green_onion", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_garlic", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:garlic", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_cabbage", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:cabbage", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_soybean", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:soybean", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_sesame", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 3, "estherserver:sesame", currencyReward = 1000, huntersPotReward = 30))

        // Mining — 3 templates (specific raw ore, 3 each)
        dailyPool.add(QuestTemplate("daily_submit_tin_ore_raw", QuestTrackingType.SUBMIT_ITEM, QuestCategory.MINING, 3, "estherserver:tin_ore_raw", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_zinc_ore_raw", QuestTrackingType.SUBMIT_ITEM, QuestCategory.MINING, 3, "estherserver:zinc_ore_raw", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_jade_raw", QuestTrackingType.SUBMIT_ITEM, QuestCategory.MINING, 3, "estherserver:jade_raw", currencyReward = 1000, huntersPotReward = 30))

        // Cooking — 8 templates (specific dish, 3 each)
        dailyPool.add(QuestTemplate("daily_submit_spinach_bibimbap", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:spinach_bibimbap", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_kimchi", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:kimchi", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_miso_soup", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:miso_soup", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_fish_stew", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:fish_stew", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_gimbap", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:gimbap", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_dumpling", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:dumpling", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_ramen", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:ramen", currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_submit_egg_rice", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 3, "estherserver:egg_rice", currencyReward = 1000, huntersPotReward = 30))

        // General — 3 templates (kill monsters)
        dailyPool.add(QuestTemplate("daily_kill_zombie", QuestTrackingType.KILL_MONSTER, QuestCategory.GENERAL, 15, targetEntityTypes = listOf("minecraft:zombie"), currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_kill_skeleton", QuestTrackingType.KILL_MONSTER, QuestCategory.GENERAL, 10, targetEntityTypes = listOf("minecraft:skeleton"), currencyReward = 1000, huntersPotReward = 30))
        dailyPool.add(QuestTemplate("daily_kill_creeper", QuestTrackingType.KILL_MONSTER, QuestCategory.GENERAL, 5, targetEntityTypes = listOf("minecraft:creeper"), currencyReward = 1000, huntersPotReward = 30))
    }

    private fun registerWeeklyQuests() {
        // Fixed 5 weekly quests — any item in category or any monster
        weeklyPool.add(QuestTemplate("weekly_submit_any_fish", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FISHING, 15, null, currencyReward = 5000, huntersPotReward = 150, isWeekly = true))
        weeklyPool.add(QuestTemplate("weekly_submit_any_crop", QuestTrackingType.SUBMIT_ITEM, QuestCategory.FARMING, 15, null, currencyReward = 5000, huntersPotReward = 150, isWeekly = true))
        weeklyPool.add(QuestTemplate("weekly_submit_any_ore", QuestTrackingType.SUBMIT_ITEM, QuestCategory.MINING, 15, null, currencyReward = 5000, huntersPotReward = 150, isWeekly = true))
        weeklyPool.add(QuestTemplate("weekly_submit_any_dish", QuestTrackingType.SUBMIT_ITEM, QuestCategory.COOKING, 15, null, currencyReward = 5000, huntersPotReward = 150, isWeekly = true))
        weeklyPool.add(QuestTemplate("weekly_kill_any_monster", QuestTrackingType.KILL_MONSTER, QuestCategory.GENERAL, 30, targetEntityTypes = listOf("minecraft:zombie", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider", "minecraft:witch", "minecraft:enderman"), currencyReward = 5000, huntersPotReward = 150, isWeekly = true))
    }

    /**
     * Select daily quests: 1 from each of 4 profession categories + 1 from GENERAL.
     * Uses per-player seed for individual random selection.
     */
    fun selectDailyQuests(seed: Long): List<QuestTemplate> {
        val random = java.util.Random(seed)
        val selected = mutableListOf<QuestTemplate>()
        val byCategory = dailyPool.groupBy { it.category }

        for (category in listOf(QuestCategory.FISHING, QuestCategory.FARMING, QuestCategory.MINING, QuestCategory.COOKING)) {
            val candidates = byCategory[category] ?: continue
            if (candidates.isNotEmpty()) {
                selected.add(candidates[random.nextInt(candidates.size)])
            }
        }

        val generalCandidates = byCategory[QuestCategory.GENERAL] ?: emptyList()
        if (generalCandidates.isNotEmpty()) {
            selected.add(generalCandidates[random.nextInt(generalCandidates.size)])
        }

        return selected.take(5)
    }

    /**
     * Returns all 5 fixed weekly quests.
     */
    fun getWeeklyQuests(): List<QuestTemplate> = weeklyPool.toList()

    fun getTemplate(id: String): QuestTemplate? {
        return dailyPool.find { it.id == id } ?: weeklyPool.find { it.id == id }
    }
}
