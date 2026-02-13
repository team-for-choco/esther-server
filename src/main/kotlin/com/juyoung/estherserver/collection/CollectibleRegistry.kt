package com.juyoung.estherserver.collection

import com.juyoung.estherserver.quality.ItemQuality
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

        // Fish (2 items x 3 qualities = 6)
        registerWithQuality("estherserver:test_fish", CollectionCategory.FISH)
        registerWithQuality("estherserver:cooked_test_fish", CollectionCategory.FISH)

        // Crops - seeds (4 items, no quality)
        registerWithoutQuality("estherserver:test_seeds", CollectionCategory.CROPS)
        registerWithoutQuality("estherserver:rice_seeds", CollectionCategory.CROPS)
        registerWithoutQuality("estherserver:red_pepper_seeds", CollectionCategory.CROPS)
        registerWithoutQuality("estherserver:spinach_seeds", CollectionCategory.CROPS)

        // Crops - produce (6 items x 3 qualities = 18)
        registerWithQuality("estherserver:test_harvest", CollectionCategory.CROPS)
        registerWithQuality("estherserver:cooked_test_harvest", CollectionCategory.CROPS)
        registerWithQuality("estherserver:rice", CollectionCategory.CROPS)
        registerWithQuality("estherserver:cooked_rice", CollectionCategory.CROPS)
        registerWithQuality("estherserver:red_pepper", CollectionCategory.CROPS)
        registerWithQuality("estherserver:spinach", CollectionCategory.CROPS)

        // Minerals (2 items x 3 qualities = 6)
        registerWithQuality("estherserver:test_ore_raw", CollectionCategory.MINERALS)
        registerWithQuality("estherserver:test_ore_ingot", CollectionCategory.MINERALS)

        // Cooking (4 items x 3 qualities = 12)
        registerWithQuality("estherserver:spinach_bibimbap", CollectionCategory.COOKING)
        registerWithQuality("estherserver:fish_stew", CollectionCategory.COOKING)
        registerWithQuality("estherserver:gimbap", CollectionCategory.COOKING)
        registerWithQuality("estherserver:harvest_bibimbap", CollectionCategory.COOKING)
    }

    private fun registerWithQuality(item: String, category: CollectionCategory, requiredCount: Int = 1) {
        val rl = ResourceLocation.parse(item)
        for (quality in ItemQuality.entries) {
            val key = CollectionKey(rl, quality)
            val def = CollectibleDefinition(key, category, requiredCount)
            definitions.add(def)
            keyToDefinition[key] = def
        }
    }

    private fun registerWithoutQuality(item: String, category: CollectionCategory, requiredCount: Int = 1) {
        val rl = ResourceLocation.parse(item)
        val key = CollectionKey(rl, null)
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
