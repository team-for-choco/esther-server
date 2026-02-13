package com.juyoung.estherserver.collection

import net.minecraft.ChatFormatting

enum class Milestone(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val color: ChatFormatting,
    val check: (CollectionData) -> Boolean,
    val progressProvider: ((CollectionData) -> Pair<Int, Int>)? = null
) {
    FIRST_DISCOVERY(
        id = "first_discovery",
        titleKey = "milestone.estherserver.first_discovery.title",
        descriptionKey = "milestone.estherserver.first_discovery.desc",
        color = ChatFormatting.GREEN,
        check = { data -> data.getCompletedCount() >= 1 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(1) to 1 }
    ),
    FISH_COMPLETE(
        id = "fish_complete",
        titleKey = "milestone.estherserver.fish_complete.title",
        descriptionKey = "milestone.estherserver.fish_complete.desc",
        color = ChatFormatting.AQUA,
        check = { data -> isCategoryComplete(data, CollectionCategory.FISH) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.FISH) }
    ),
    CROPS_COMPLETE(
        id = "crops_complete",
        titleKey = "milestone.estherserver.crops_complete.title",
        descriptionKey = "milestone.estherserver.crops_complete.desc",
        color = ChatFormatting.DARK_GREEN,
        check = { data -> isCategoryComplete(data, CollectionCategory.CROPS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.CROPS) }
    ),
    MINERALS_COMPLETE(
        id = "minerals_complete",
        titleKey = "milestone.estherserver.minerals_complete.title",
        descriptionKey = "milestone.estherserver.minerals_complete.desc",
        color = ChatFormatting.GRAY,
        check = { data -> isCategoryComplete(data, CollectionCategory.MINERALS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.MINERALS) }
    ),
    COOKING_COMPLETE(
        id = "cooking_complete",
        titleKey = "milestone.estherserver.cooking_complete.title",
        descriptionKey = "milestone.estherserver.cooking_complete.desc",
        color = ChatFormatting.YELLOW,
        check = { data -> isCategoryComplete(data, CollectionCategory.COOKING) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.COOKING) }
    ),
    HALF_COMPLETE(
        id = "half_complete",
        titleKey = "milestone.estherserver.half_complete.title",
        descriptionKey = "milestone.estherserver.half_complete.desc",
        color = ChatFormatting.LIGHT_PURPLE,
        check = { data -> data.getCompletedCount() >= 23 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(23) to 23 }
    ),
    ALL_COMPLETE(
        id = "all_complete",
        titleKey = "milestone.estherserver.all_complete.title",
        descriptionKey = "milestone.estherserver.all_complete.desc",
        color = ChatFormatting.GOLD,
        check = { data -> data.getCompletedCount() >= CollectibleRegistry.getTotalCount() },
        progressProvider = { data -> data.getCompletedCount() to CollectibleRegistry.getTotalCount() }
    );

    companion object {
        private val BY_ID = entries.associateBy { it.id }

        fun byId(id: String): Milestone? = BY_ID[id]

        private fun isCategoryComplete(data: CollectionData, category: CollectionCategory): Boolean {
            val defs = CollectibleRegistry.getDefinitionsByCategory(category)
            return defs.all { data.isComplete(it.key) }
        }

        private fun categoryProgress(data: CollectionData, category: CollectionCategory): Pair<Int, Int> {
            val defs = CollectibleRegistry.getDefinitionsByCategory(category)
            val completed = defs.count { data.isComplete(it.key) }
            return completed to defs.size
        }
    }
}
