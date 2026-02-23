package com.juyoung.estherserver.profession

import net.minecraft.resources.ResourceLocation

object ProfessionBonusHelper {

    // --- Progressive Per-Level Bonuses ---

    /** Mining speed bonus: +1% per level, Lv50 = +50% */
    fun getMiningSpeedBonus(profLevel: Int): Float =
        (profLevel.coerceIn(0, 50) * 0.01f)

    /** Fishing lure reduction: -1 tick per level, Lv50 = -50 ticks */
    fun getFishingLureReduction(profLevel: Int): Int =
        profLevel.coerceIn(0, 50)

    /** Crop growth multiplier: -1% per level, Lv50 = 0.5x */
    fun getCropGrowthMultiplier(profLevel: Int): Float =
        1.0f - (profLevel.coerceIn(0, 50) * 0.01f)

    /** Cooking time reduction: -0.05s per level, Lv50 = -2.5s */
    fun getCookingTimeReduction(profLevel: Int): Float =
        profLevel.coerceIn(0, 50) * 0.05f

    /** Profession inventory slots: 5 slots per 10 levels starting at Lv10, max 25 */
    fun getInventorySlots(profLevel: Int): Int {
        if (profLevel < 10) return 0
        return ((profLevel / 10) * 5).coerceAtMost(25)
    }

    // --- Pickaxe Mining Tier by Enhancement Level ---

    /** Mining tier tag for the pickaxe enhancement level */
    fun getPickaxeMiningTier(equipLevel: Int): String = when {
        equipLevel >= 3 -> "minecraft:needs_diamond_tool"
        equipLevel >= 2 -> "minecraft:needs_iron_tool"
        equipLevel >= 1 -> "minecraft:needs_stone_tool"
        else -> ""  // Lv0 = wood tier (no tag needed)
    }

    // --- Ore Grade Classification ---

    enum class OreGrade(val enhancementStoneDropRate: Float) {
        COMMON(0.005f),   // 0.5%
        ADVANCED(0.01f),  // 1%
        RARE(0.02f)       // 2%
    }

    private val oreGradeMap = mutableMapOf<ResourceLocation, OreGrade>()

    fun initOreGrades() {
        // Common ores
        registerOreGrade("minecraft:coal", OreGrade.COMMON)
        registerOreGrade("minecraft:raw_copper", OreGrade.COMMON)
        registerOreGrade("minecraft:redstone", OreGrade.COMMON)
        registerOreGrade("minecraft:raw_iron", OreGrade.COMMON)
        registerOreGrade("minecraft:lapis_lazuli", OreGrade.COMMON)
        registerOreGrade("minecraft:quartz", OreGrade.COMMON)
        registerOreGrade("minecraft:amethyst_shard", OreGrade.COMMON)

        // Advanced ores
        registerOreGrade("minecraft:raw_gold", OreGrade.ADVANCED)
        registerOreGrade("minecraft:diamond", OreGrade.ADVANCED)
        registerOreGrade("minecraft:emerald", OreGrade.ADVANCED)

        // Rare ores (custom)
        registerOreGrade("estherserver:test_ore_raw", OreGrade.RARE)
    }

    private fun registerOreGrade(item: String, grade: OreGrade) {
        oreGradeMap[ResourceLocation.parse(item)] = grade
    }

    fun getOreGrade(itemId: ResourceLocation): OreGrade? = oreGradeMap[itemId]

    // --- Fishing Enhancement Stone Drop Rate ---
    const val FISHING_ENHANCEMENT_STONE_DROP_RATE = 0.01f  // 1%

    // --- Recipe / Fish / Crop Grade ---

    enum class ContentGrade {
        COMMON, ADVANCED, RARE
    }

    private val fishGradeMap = mutableMapOf<ResourceLocation, ContentGrade>()
    private val cropGradeMap = mutableMapOf<ResourceLocation, ContentGrade>()

    fun initContentGrades() {
        // Fish grades (to be expanded)
        registerFishGrade("estherserver:test_fish", ContentGrade.COMMON)

        // Crop grades (to be expanded)
        registerCropGrade("estherserver:test_harvest", ContentGrade.COMMON)
        registerCropGrade("estherserver:rice", ContentGrade.COMMON)
        registerCropGrade("estherserver:red_pepper", ContentGrade.COMMON)
        registerCropGrade("estherserver:spinach", ContentGrade.COMMON)
    }

    private fun registerFishGrade(item: String, grade: ContentGrade) {
        fishGradeMap[ResourceLocation.parse(item)] = grade
    }

    private fun registerCropGrade(item: String, grade: ContentGrade) {
        cropGradeMap[ResourceLocation.parse(item)] = grade
    }

    fun getFishGrade(itemId: ResourceLocation): ContentGrade? = fishGradeMap[itemId]
    fun getCropGrade(itemId: ResourceLocation): ContentGrade? = cropGradeMap[itemId]

    /** Max fish grade the fishing rod can catch based on enhancement level */
    fun getMaxFishGrade(equipLevel: Int): ContentGrade = when {
        equipLevel >= 2 -> ContentGrade.ADVANCED
        equipLevel >= 1 -> ContentGrade.COMMON
        else -> ContentGrade.COMMON  // Lv0 = no custom fish
    }

    /** Whether the fishing rod can catch custom fish at all */
    fun canCatchCustomFish(equipLevel: Int): Boolean = equipLevel >= 1

    /** Max crop grade the hoe can harvest based on enhancement level */
    fun getMaxCropGrade(equipLevel: Int): ContentGrade = when {
        equipLevel >= 5 -> ContentGrade.RARE
        equipLevel >= 2 -> ContentGrade.ADVANCED
        equipLevel >= 1 -> ContentGrade.COMMON
        else -> ContentGrade.COMMON  // Lv0 = no custom crops
    }

    /** Whether the hoe can harvest custom crops at all */
    fun canHarvestCustomCrops(equipLevel: Int): Boolean = equipLevel >= 1

    /** Max recipe grade the cooking tool can use based on enhancement level */
    fun getMaxRecipeGrade(equipLevel: Int): ContentGrade = when {
        equipLevel >= 5 -> ContentGrade.RARE
        equipLevel >= 1 -> ContentGrade.ADVANCED
        else -> ContentGrade.COMMON
    }
}
