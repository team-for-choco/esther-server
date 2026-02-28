package com.juyoung.estherserver.collection

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.ChatFormatting
import net.minecraft.world.item.ItemStack

data class MilestoneReward(
    val titleKey: String?,
    val items: List<ItemStack> = emptyList(),
    val currencyReward: Long = 0
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
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.first_discovery.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1)),
            currencyReward = 500
        )
    ),
    TEN_COMPLETE(
        id = "ten_complete",
        titleKey = "milestone.estherserver.ten_complete.title",
        descriptionKey = "milestone.estherserver.ten_complete.desc",
        color = ChatFormatting.GREEN,
        check = { data -> data.getCompletedCount() >= 10 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(10) to 10 },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.ten_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1)),
            currencyReward = 1000
        )
    ),
    TWENTYFIVE_COMPLETE(
        id = "twentyfive_complete",
        titleKey = "milestone.estherserver.twentyfive_complete.title",
        descriptionKey = "milestone.estherserver.twentyfive_complete.desc",
        color = ChatFormatting.DARK_GREEN,
        check = { data -> data.getCompletedCount() >= 25 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(25) to 25 },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.twentyfive_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1)),
            currencyReward = 1500
        )
    ),
    FIFTY_COMPLETE(
        id = "fifty_complete",
        titleKey = "milestone.estherserver.fifty_complete.title",
        descriptionKey = "milestone.estherserver.fifty_complete.desc",
        color = ChatFormatting.DARK_AQUA,
        check = { data -> data.getCompletedCount() >= 50 },
        progressProvider = { data -> data.getCompletedCount().coerceAtMost(50) to 50 },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.fifty_complete.title",
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1)),
            currencyReward = 2000
        )
    ),
    FISH_COMPLETE(
        id = "fish_complete",
        titleKey = "milestone.estherserver.fish_complete.title",
        descriptionKey = "milestone.estherserver.fish_complete.desc",
        color = ChatFormatting.AQUA,
        check = { data -> isCategoryComplete(data, CollectionCategory.FISH) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.FISH) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.fish_complete.title",
            items = listOf(ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 1)),
            currencyReward = 2000
        )
    ),
    CROPS_COMPLETE(
        id = "crops_complete",
        titleKey = "milestone.estherserver.crops_complete.title",
        descriptionKey = "milestone.estherserver.crops_complete.desc",
        color = ChatFormatting.DARK_GREEN,
        check = { data -> isCategoryComplete(data, CollectionCategory.CROPS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.CROPS) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.crops_complete.title",
            items = listOf(ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 2)),
            currencyReward = 3000
        )
    ),
    MINERALS_COMPLETE(
        id = "minerals_complete",
        titleKey = "milestone.estherserver.minerals_complete.title",
        descriptionKey = "milestone.estherserver.minerals_complete.desc",
        color = ChatFormatting.GRAY,
        check = { data -> isCategoryComplete(data, CollectionCategory.MINERALS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.MINERALS) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.minerals_complete.title",
            items = listOf(ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 1)),
            currencyReward = 2000
        )
    ),
    COOKING_COMPLETE(
        id = "cooking_complete",
        titleKey = "milestone.estherserver.cooking_complete.title",
        descriptionKey = "milestone.estherserver.cooking_complete.desc",
        color = ChatFormatting.YELLOW,
        check = { data -> isCategoryComplete(data, CollectionCategory.COOKING) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.COOKING) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.cooking_complete.title",
            items = listOf(ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 2)),
            currencyReward = 3000
        )
    ),
    BLOCKS_COMPLETE(
        id = "blocks_complete",
        titleKey = "milestone.estherserver.blocks_complete.title",
        descriptionKey = "milestone.estherserver.blocks_complete.desc",
        color = ChatFormatting.WHITE,
        check = { data -> isCategoryComplete(data, CollectionCategory.BLOCKS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.BLOCKS) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.blocks_complete.title",
            items = listOf(
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 3),
                ItemStack(EstherServerMod.LAND_DEED.get(), 2)
            ),
            currencyReward = 10000
        )
    ),
    EQUIPMENT_COMPLETE(
        id = "equipment_complete",
        titleKey = "milestone.estherserver.equipment_complete.title",
        descriptionKey = "milestone.estherserver.equipment_complete.desc",
        color = ChatFormatting.RED,
        check = { data -> isCategoryComplete(data, CollectionCategory.EQUIPMENT) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.EQUIPMENT) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.equipment_complete.title",
            items = listOf(
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 3),
                ItemStack(EstherServerMod.ENHANCEMENT_STONE.get(), 2)
            ),
            currencyReward = 8000
        )
    ),
    FOOD_COMPLETE(
        id = "food_complete",
        titleKey = "milestone.estherserver.food_complete.title",
        descriptionKey = "milestone.estherserver.food_complete.desc",
        color = ChatFormatting.GOLD,
        check = { data -> isCategoryComplete(data, CollectionCategory.FOOD) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.FOOD) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.food_complete.title",
            items = listOf(
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 2),
                ItemStack(EstherServerMod.ENHANCEMENT_STONE.get(), 1)
            ),
            currencyReward = 5000
        )
    ),
    MATERIALS_COMPLETE(
        id = "materials_complete",
        titleKey = "milestone.estherserver.materials_complete.title",
        descriptionKey = "milestone.estherserver.materials_complete.desc",
        color = ChatFormatting.DARK_PURPLE,
        check = { data -> isCategoryComplete(data, CollectionCategory.MATERIALS) },
        progressProvider = { data -> categoryProgress(data, CollectionCategory.MATERIALS) },
        reward = MilestoneReward(
            titleKey = "milestone.estherserver.materials_complete.title",
            items = listOf(
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 3),
                ItemStack(EstherServerMod.LAND_DEED.get(), 2)
            ),
            currencyReward = 10000
        )
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
            items = listOf(ItemStack(EstherServerMod.LAND_DEED.get(), 1)),
            currencyReward = 3000
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
            items = listOf(
                ItemStack(EstherServerMod.LAND_DEED.get(), 2),
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 3)
            ),
            currencyReward = 5000
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
            items = listOf(
                ItemStack(EstherServerMod.LAND_DEED.get(), 5),
                ItemStack(EstherServerMod.DRAW_TICKET_NORMAL.get(), 5),
                ItemStack(EstherServerMod.ENHANCEMENT_STONE.get(), 3)
            ),
            currencyReward = 15000
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
