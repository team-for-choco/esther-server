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
        // Common ores (vanilla)
        registerOreGrade("minecraft:coal", OreGrade.COMMON)
        registerOreGrade("minecraft:raw_copper", OreGrade.COMMON)
        registerOreGrade("minecraft:redstone", OreGrade.COMMON)
        registerOreGrade("minecraft:raw_iron", OreGrade.COMMON)
        registerOreGrade("minecraft:lapis_lazuli", OreGrade.COMMON)
        registerOreGrade("minecraft:quartz", OreGrade.COMMON)
        registerOreGrade("minecraft:amethyst_shard", OreGrade.COMMON)

        // Advanced ores (vanilla)
        registerOreGrade("minecraft:raw_gold", OreGrade.ADVANCED)
        registerOreGrade("minecraft:diamond", OreGrade.ADVANCED)
        registerOreGrade("minecraft:emerald", OreGrade.ADVANCED)
    }

    fun registerOreGrade(item: String, grade: OreGrade) {
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
    private val recipeGradeMap = mutableMapOf<ResourceLocation, ContentGrade>()

    fun initContentGrades() {
        // Fish grades
        registerFishGrade("estherserver:crucian_carp", ContentGrade.COMMON)
        registerFishGrade("estherserver:sweetfish", ContentGrade.COMMON)
        registerFishGrade("estherserver:mackerel", ContentGrade.COMMON)
        registerFishGrade("estherserver:squid_catch", ContentGrade.COMMON)
        registerFishGrade("estherserver:anchovy", ContentGrade.COMMON)
        registerFishGrade("estherserver:shrimp", ContentGrade.COMMON)
        registerFishGrade("estherserver:clam", ContentGrade.COMMON)
        registerFishGrade("estherserver:salmon_catch", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:sea_bream", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:eel", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:octopus", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:hairtail", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:yellowtail", ContentGrade.ADVANCED)
        registerFishGrade("estherserver:bluefin_tuna", ContentGrade.RARE)
        registerFishGrade("estherserver:blowfish", ContentGrade.RARE)
        registerFishGrade("estherserver:abalone", ContentGrade.RARE)
        registerFishGrade("estherserver:king_crab", ContentGrade.RARE)
        registerFishGrade("estherserver:sea_urchin", ContentGrade.RARE)
        registerFishGrade("estherserver:sturgeon", ContentGrade.RARE)

        // Crop grades - Common
        registerCropGrade("estherserver:rice", ContentGrade.COMMON)
        registerCropGrade("estherserver:red_pepper", ContentGrade.COMMON)
        registerCropGrade("estherserver:spinach", ContentGrade.COMMON)
        registerCropGrade("estherserver:green_onion", ContentGrade.COMMON)
        registerCropGrade("estherserver:garlic", ContentGrade.COMMON)
        registerCropGrade("estherserver:cabbage", ContentGrade.COMMON)
        registerCropGrade("estherserver:soybean", ContentGrade.COMMON)
        registerCropGrade("estherserver:sesame", ContentGrade.COMMON)
        // Crop grades - Advanced
        registerCropGrade("estherserver:ginger", ContentGrade.ADVANCED)
        registerCropGrade("estherserver:perilla", ContentGrade.ADVANCED)
        registerCropGrade("estherserver:lotus_root", ContentGrade.ADVANCED)
        registerCropGrade("estherserver:shiitake", ContentGrade.ADVANCED)
        registerCropGrade("estherserver:bamboo_shoot", ContentGrade.ADVANCED)
        registerCropGrade("estherserver:wasabi", ContentGrade.ADVANCED)
        // Crop grades - Rare
        registerCropGrade("estherserver:ginseng", ContentGrade.RARE)
        registerCropGrade("estherserver:truffle", ContentGrade.RARE)
        registerCropGrade("estherserver:saffron", ContentGrade.RARE)
        registerCropGrade("estherserver:matsutake", ContentGrade.RARE)
        registerCropGrade("estherserver:yuzu", ContentGrade.RARE)
        registerCropGrade("estherserver:green_tea", ContentGrade.RARE)
    }

    fun registerFishGrade(item: String, grade: ContentGrade) {
        fishGradeMap[ResourceLocation.parse(item)] = grade
    }

    fun registerCropGrade(item: String, grade: ContentGrade) {
        cropGradeMap[ResourceLocation.parse(item)] = grade
    }

    fun registerRecipeGrade(item: String, grade: ContentGrade) {
        recipeGradeMap[ResourceLocation.parse(item)] = grade
    }

    fun getFishGrade(itemId: ResourceLocation): ContentGrade? = fishGradeMap[itemId]
    fun getCropGrade(itemId: ResourceLocation): ContentGrade? = cropGradeMap[itemId]
    fun getRecipeGrade(itemId: ResourceLocation): ContentGrade? = recipeGradeMap[itemId]

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
