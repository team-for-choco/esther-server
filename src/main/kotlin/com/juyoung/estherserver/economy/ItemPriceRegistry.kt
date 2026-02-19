package com.juyoung.estherserver.economy

import com.juyoung.estherserver.merchant.ShopCategory
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

data class PriceEntry(val price: Long, val category: ShopCategory)

object ItemPriceRegistry {
    private val prices = mutableMapOf<ResourceLocation, PriceEntry>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        registerCustomItemPrices()
        registerVanillaItemPrices()
    }

    private fun registerCustomItemPrices() {
        // Fish
        register("estherserver:test_fish", 5, ShopCategory.FOOD)
        register("estherserver:cooked_test_fish", 8, ShopCategory.FOOD)

        // Seeds (no quality)
        register("estherserver:test_seeds", 1, ShopCategory.SEEDS)
        register("estherserver:rice_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:red_pepper_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:spinach_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:special_farmland", 10, ShopCategory.SEEDS)

        // Harvests
        register("estherserver:test_harvest", 3, ShopCategory.FOOD)
        register("estherserver:cooked_test_harvest", 6, ShopCategory.FOOD)
        register("estherserver:rice", 4, ShopCategory.FOOD)
        register("estherserver:cooked_rice", 7, ShopCategory.FOOD)
        register("estherserver:red_pepper", 4, ShopCategory.FOOD)
        register("estherserver:spinach", 3, ShopCategory.FOOD)

        // Minerals
        register("estherserver:test_ore_raw", 8, ShopCategory.MINERALS)
        register("estherserver:test_ore_ingot", 12, ShopCategory.MINERALS)

        // Cooking dishes
        register("estherserver:spinach_bibimbap", 20, ShopCategory.FOOD)
        register("estherserver:fish_stew", 25, ShopCategory.FOOD)
        register("estherserver:gimbap", 25, ShopCategory.FOOD)
        register("estherserver:harvest_bibimbap", 30, ShopCategory.FOOD)
    }

    private fun registerVanillaItemPrices() {
        // Fish
        register("minecraft:cod", 3, ShopCategory.FOOD)
        register("minecraft:salmon", 4, ShopCategory.FOOD)
        register("minecraft:tropical_fish", 8, ShopCategory.FOOD)
        register("minecraft:pufferfish", 6, ShopCategory.FOOD)
        register("minecraft:cooked_cod", 5, ShopCategory.FOOD)
        register("minecraft:cooked_salmon", 6, ShopCategory.FOOD)

        // Crops
        register("minecraft:wheat", 2, ShopCategory.FOOD)
        register("minecraft:potato", 2, ShopCategory.FOOD)
        register("minecraft:carrot", 2, ShopCategory.FOOD)
        register("minecraft:beetroot", 2, ShopCategory.FOOD)
        register("minecraft:melon_slice", 1, ShopCategory.FOOD)
        register("minecraft:pumpkin", 3, ShopCategory.FOOD)
        register("minecraft:sugar_cane", 1, ShopCategory.FOOD)
        register("minecraft:sweet_berries", 1, ShopCategory.FOOD)
        register("minecraft:apple", 2, ShopCategory.FOOD)
        register("minecraft:baked_potato", 4, ShopCategory.FOOD)
        register("minecraft:bread", 4, ShopCategory.FOOD)

        // Minerals
        register("minecraft:coal", 2, ShopCategory.MINERALS)
        register("minecraft:raw_iron", 5, ShopCategory.MINERALS)
        register("minecraft:raw_gold", 8, ShopCategory.MINERALS)
        register("minecraft:raw_copper", 3, ShopCategory.MINERALS)
        register("minecraft:iron_ingot", 10, ShopCategory.MINERALS)
        register("minecraft:gold_ingot", 15, ShopCategory.MINERALS)
        register("minecraft:copper_ingot", 5, ShopCategory.MINERALS)
        register("minecraft:diamond", 50, ShopCategory.MINERALS)
        register("minecraft:emerald", 30, ShopCategory.MINERALS)
        register("minecraft:lapis_lazuli", 5, ShopCategory.MINERALS)
        register("minecraft:redstone", 3, ShopCategory.MINERALS)
        register("minecraft:quartz", 4, ShopCategory.MINERALS)
        register("minecraft:amethyst_shard", 4, ShopCategory.MINERALS)
        register("minecraft:netherite_scrap", 100, ShopCategory.MINERALS)

        // Mob drops
        register("minecraft:leather", 3, ShopCategory.SPECIAL)
        register("minecraft:string", 2, ShopCategory.SPECIAL)
        register("minecraft:feather", 1, ShopCategory.SPECIAL)
        register("minecraft:bone", 1, ShopCategory.SPECIAL)
        register("minecraft:gunpowder", 3, ShopCategory.SPECIAL)
        register("minecraft:ender_pearl", 15, ShopCategory.SPECIAL)
        register("minecraft:blaze_rod", 10, ShopCategory.SPECIAL)
        register("minecraft:ghast_tear", 20, ShopCategory.SPECIAL)
        register("minecraft:slime_ball", 3, ShopCategory.SPECIAL)
        register("minecraft:phantom_membrane", 8, ShopCategory.SPECIAL)
        register("minecraft:ink_sac", 2, ShopCategory.SPECIAL)
        register("minecraft:glow_ink_sac", 5, ShopCategory.SPECIAL)
    }

    private fun register(item: String, price: Long, category: ShopCategory) {
        prices[ResourceLocation.parse(item)] = PriceEntry(price, category)
    }

    fun getBasePrice(itemId: ResourceLocation): Long? = prices[itemId]?.price

    fun getPrice(stack: ItemStack): Long? {
        val itemId = stack.itemHolder.unwrapKey().orElse(null)?.location() ?: return null
        val basePrice = prices[itemId]?.price ?: return null
        val quality = stack.get(ModDataComponents.ITEM_QUALITY.get())
        val multiplier = getQualityMultiplier(quality)
        return (basePrice * multiplier).toLong()
    }

    fun getCategory(itemId: ResourceLocation): ShopCategory? = prices[itemId]?.category

    private fun getQualityMultiplier(quality: ItemQuality?): Double {
        return when (quality) {
            ItemQuality.FINE -> 1.5
            ItemQuality.RARE -> 3.0
            else -> 1.0
        }
    }
}
