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

        // Fish (2 items)
        register("estherserver:test_fish", CollectionCategory.FISH)
        register("estherserver:cooked_test_fish", CollectionCategory.FISH)

        // Crops - seeds (4 items)
        register("estherserver:test_seeds", CollectionCategory.CROPS)
        register("estherserver:rice_seeds", CollectionCategory.CROPS)
        register("estherserver:red_pepper_seeds", CollectionCategory.CROPS)
        register("estherserver:spinach_seeds", CollectionCategory.CROPS)

        // Crops - produce (6 items)
        register("estherserver:test_harvest", CollectionCategory.CROPS)
        register("estherserver:cooked_test_harvest", CollectionCategory.CROPS)
        register("estherserver:rice", CollectionCategory.CROPS)
        register("estherserver:cooked_rice", CollectionCategory.CROPS)
        register("estherserver:red_pepper", CollectionCategory.CROPS)
        register("estherserver:spinach", CollectionCategory.CROPS)

        // Minerals (2 items)
        register("estherserver:test_ore_raw", CollectionCategory.MINERALS)
        register("estherserver:test_ore_ingot", CollectionCategory.MINERALS)

        // Cooking (4 items)
        register("estherserver:spinach_bibimbap", CollectionCategory.COOKING)
        register("estherserver:fish_stew", CollectionCategory.COOKING)
        register("estherserver:gimbap", CollectionCategory.COOKING)
        register("estherserver:harvest_bibimbap", CollectionCategory.COOKING)
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
