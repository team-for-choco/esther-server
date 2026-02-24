package com.juyoung.estherserver.collection

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.ChatFormatting
import net.minecraft.world.item.ItemStack

data class MilestoneReward(
    val titleKey: String?,
    val items: List<ItemStack> = emptyList()
)

enum class Milestone(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val color: ChatFormatting,
    val check: (CollectionData) -> Boolean,
    val progressProvider: ((CollectionData) -> Pair<Int, Int>)? = null,
    val reward: MilestoneReward
) {
    FIRST_DISCOVERY(
        id = "first_discovery",
        titleKey = "milestone.estherserver.first_discovery.title",
        descriptionKey = "milestone.estherserver.first_discovery.desc",
        color = ChatFormatting.GREEN,
        check = { data -> data.getCompletedCount() >= 1 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(1) to 1 },
        reward = MilestoneReward(titleKey = "milestone.estherserver.first_discovery.title")
    ),
    FISH_COMPLETE(
        id = "fish_complete",
        titleKey = "milestone.estherserver.fish_complete.title",
        descriptionKey = "milestone.estherserver.fish_complete.desc",
        color = ChatFormatting.AQUA,
        check = { data -> isCategoryComplete(data, CollectionCategory.FISH) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.FISH) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.fish_complete.title")
    ),
    CROPS_COMPLETE(
        id = "crops_complete",
        titleKey = "milestone.estherserver.crops_complete.title",
        descriptionKey = "milestone.estherserver.crops_complete.desc",
        color = ChatFormatting.DARK_GREEN,
        check = { data -> isCategoryComplete(data, CollectionCategory.CROPS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.CROPS) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.crops_complete.title")
    ),
    MINERALS_COMPLETE(
        id = "minerals_complete",
        titleKey = "milestone.estherserver.minerals_complete.title",
        descriptionKey = "milestone.estherserver.minerals_complete.desc",
        color = ChatFormatting.GRAY,
        check = { data -> isCategoryComplete(data, CollectionCategory.MINERALS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.MINERALS) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.minerals_complete.title")
    ),
    COOKING_COMPLETE(
        id = "cooking_complete",
        titleKey = "milestone.estherserver.cooking_complete.title",
        descriptionKey = "milestone.estherserver.cooking_complete.desc",
        color = ChatFormatting.YELLOW,
        check = { data -> isCategoryComplete(data, CollectionCategory.COOKING) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.COOKING) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.cooking_complete.title")
    ),
    BLOCKS_COMPLETE(
        id = "blocks_complete",
        titleKey = "milestone.estherserver.blocks_complete.title",
        descriptionKey = "milestone.estherserver.blocks_complete.desc",
        color = ChatFormatting.WHITE,
        check = { data -> isCategoryComplete(data, CollectionCategory.BLOCKS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.BLOCKS) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.blocks_complete.title")
    ),
    EQUIPMENT_COMPLETE(
        id = "equipment_complete",
        titleKey = "milestone.estherserver.equipment_complete.title",
        descriptionKey = "milestone.estherserver.equipment_complete.desc",
        color = ChatFormatting.RED,
        check = { data -> isCategoryComplete(data, CollectionCategory.EQUIPMENT) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.EQUIPMENT) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.equipment_complete.title")
    ),
    FOOD_COMPLETE(
        id = "food_complete",
        titleKey = "milestone.estherserver.food_complete.title",
        descriptionKey = "milestone.estherserver.food_complete.desc",
        color = ChatFormatting.GOLD,
        check = { data -> isCategoryComplete(data, CollectionCategory.FOOD) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.FOOD) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.food_complete.title")
    ),
    MATERIALS_COMPLETE(
        id = "materials_complete",
        titleKey = "milestone.estherserver.materials_complete.title",
        descriptionKey = "milestone.estherserver.materials_complete.desc",
        color = ChatFormatting.DARK_PURPLE,
        check = { data -> isCategoryComplete(data, CollectionCategory.MATERIALS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.MATERIALS) },
        reward = MilestoneReward(titleKey = "milestone.estherserver.materials_complete.title")
    ),
    HUNDRED_COMPLETE(
        id = "hundred_complete",
        titleKey = "milestone.estherserver.hundred_complete.title",
        descriptionKey = "milestone.estherserver.hundred_complete.desc",
        color = ChatFormatting.DARK_AQUA,
        check = { data -> data.getCompletedCount() >= 100 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(100) to 100 },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.hundred_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1))
        )
    ),
    HALF_COMPLETE(
        id = "half_complete",
        titleKey = "milestone.estherserver.half_complete.title",
        descriptionKey = "milestone.estherserver.half_complete.desc",
        color = ChatFormatting.LIGHT_PURPLE,
        check = { data -> data.getCompletedCount() >= CollectibleRegistry.getTotalCount() / 2 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(CollectibleRegistry.getTotalCount() / 2) to CollectibleRegistry.getTotalCount() / 2 },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.half_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 2))
        )
    ),
    ALL_COMPLETE(
        id = "all_complete",
        titleKey = "milestone.estherserver.all_complete.title",
        descriptionKey = "milestone.estherserver.all_complete.desc",
        color = ChatFormatting.GOLD,
        check = { data -> data.getCompletedCount() >= CollectibleRegistry.getTotalCount() },
        progressProvider = { data -> data.getCompletedCount() to CollectibleRegistry.getTotalCount() },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.all_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 5))
        )
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
