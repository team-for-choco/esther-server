package com.juyoung.estherserver.collection

import net.minecraft.resources.ResourceLocation

enum class CollectionCategory(val translationKey: String) {
    FISH("gui.estherserver.collection.category.fish"),
    CROPS("gui.estherserver.collection.category.crops"),
    MINERALS("gui.estherserver.collection.category.minerals"),
    COOKING("gui.estherserver.collection.category.cooking")
}

data class CollectibleDefinition(
    val key: CollectionKey,
    val category: CollectionCategory,
    val requiredCount: Int = 1
)

object CollectibleRegistry {
    private val definitions = mutableListOf<CollectibleDefinition>()
    private val keyToDefinition = mutableMapOf<CollectionKey, CollectibleDefinition>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true

        // Fish - Common (7)
        register("estherserver:crucian_carp", CollectionCategory.FISH)
        register("estherserver:sweetfish", CollectionCategory.FISH)
        register("estherserver:mackerel", CollectionCategory.FISH)
        register("estherserver:squid_catch", CollectionCategory.FISH)
        register("estherserver:anchovy", CollectionCategory.FISH)
        register("estherserver:shrimp", CollectionCategory.FISH)
        register("estherserver:clam", CollectionCategory.FISH)

        // Fish - Advanced (6)
        register("estherserver:salmon_catch", CollectionCategory.FISH)
        register("estherserver:sea_bream", CollectionCategory.FISH)
        register("estherserver:eel", CollectionCategory.FISH)
        register("estherserver:octopus", CollectionCategory.FISH)
        register("estherserver:hairtail", CollectionCategory.FISH)
        register("estherserver:yellowtail", CollectionCategory.FISH)

        // Fish - Rare (6)
        register("estherserver:bluefin_tuna", CollectionCategory.FISH)
        register("estherserver:blowfish", CollectionCategory.FISH)
        register("estherserver:abalone", CollectionCategory.FISH)
        register("estherserver:king_crab", CollectionCategory.FISH)
        register("estherserver:sea_urchin", CollectionCategory.FISH)
        register("estherserver:sturgeon", CollectionCategory.FISH)

        // Crops - seeds (20)
        register("estherserver:rice_seeds", CollectionCategory.CROPS)
        register("estherserver:red_pepper_seeds", CollectionCategory.CROPS)
        register("estherserver:spinach_seeds", CollectionCategory.CROPS)
        register("estherserver:green_onion_seeds", CollectionCategory.CROPS)
        register("estherserver:garlic_seeds", CollectionCategory.CROPS)
        register("estherserver:cabbage_seeds", CollectionCategory.CROPS)
        register("estherserver:soybean_seeds", CollectionCategory.CROPS)
        register("estherserver:sesame_seeds", CollectionCategory.CROPS)
        register("estherserver:ginger_seeds", CollectionCategory.CROPS)
        register("estherserver:perilla_seeds", CollectionCategory.CROPS)
        register("estherserver:lotus_root_seeds", CollectionCategory.CROPS)
        register("estherserver:shiitake_seeds", CollectionCategory.CROPS)
        register("estherserver:bamboo_shoot_seeds", CollectionCategory.CROPS)
        register("estherserver:wasabi_seeds", CollectionCategory.CROPS)
        register("estherserver:ginseng_seeds", CollectionCategory.CROPS)
        register("estherserver:truffle_seeds", CollectionCategory.CROPS)
        register("estherserver:saffron_seeds", CollectionCategory.CROPS)
        register("estherserver:matsutake_seeds", CollectionCategory.CROPS)
        register("estherserver:yuzu_seeds", CollectionCategory.CROPS)
        register("estherserver:green_tea_seeds", CollectionCategory.CROPS)

        // Crops - produce (21 including cooked_rice)
        register("estherserver:rice", CollectionCategory.CROPS)
        register("estherserver:cooked_rice", CollectionCategory.CROPS)
        register("estherserver:red_pepper", CollectionCategory.CROPS)
        register("estherserver:spinach", CollectionCategory.CROPS)
        register("estherserver:green_onion", CollectionCategory.CROPS)
        register("estherserver:garlic", CollectionCategory.CROPS)
        register("estherserver:cabbage", CollectionCategory.CROPS)
        register("estherserver:soybean", CollectionCategory.CROPS)
        register("estherserver:sesame", CollectionCategory.CROPS)
        register("estherserver:ginger", CollectionCategory.CROPS)
        register("estherserver:perilla", CollectionCategory.CROPS)
        register("estherserver:lotus_root", CollectionCategory.CROPS)
        register("estherserver:shiitake", CollectionCategory.CROPS)
        register("estherserver:bamboo_shoot", CollectionCategory.CROPS)
        register("estherserver:wasabi", CollectionCategory.CROPS)
        register("estherserver:ginseng", CollectionCategory.CROPS)
        register("estherserver:truffle", CollectionCategory.CROPS)
        register("estherserver:saffron", CollectionCategory.CROPS)
        register("estherserver:matsutake", CollectionCategory.CROPS)
        register("estherserver:yuzu", CollectionCategory.CROPS)
        register("estherserver:green_tea", CollectionCategory.CROPS)

        // Cooking (3 items, existing)
        register("estherserver:spinach_bibimbap", CollectionCategory.COOKING)
        register("estherserver:fish_stew", CollectionCategory.COOKING)
        register("estherserver:gimbap", CollectionCategory.COOKING)
    }

    private fun register(item: String, category: CollectionCategory, requiredCount: Int = 1) {
        val rl = ResourceLocation.parse(item)
        val key = CollectionKey(rl)
        val def = CollectibleDefinition(key, category, requiredCount)
        definitions.add(def)
        keyToDefinition[key] = def
    }

    fun isValidKey(key: CollectionKey): Boolean = keyToDefinition.containsKey(key)

    fun getDefinition(key: CollectionKey): CollectibleDefinition? = keyToDefinition[key]

    fun getRequiredCount(key: CollectionKey): Int = keyToDefinition[key]?.requiredCount ?: 1

    fun getAllDefinitions(): List<CollectibleDefinition> = definitions.toList()

    fun getDefinitionsByCategory(category: CollectionCategory): List<CollectibleDefinition> =
        definitions.filter { it.category == category }

    fun getTotalCount(): Int = definitions.size
}
