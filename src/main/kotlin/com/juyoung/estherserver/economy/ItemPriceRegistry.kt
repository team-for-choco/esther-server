package com.juyoung.estherserver.economy

import com.juyoung.estherserver.merchant.ShopCategory
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
        // Fish - Common
        register("estherserver:crucian_carp", 5, ShopCategory.FOOD)
        register("estherserver:sweetfish", 5, ShopCategory.FOOD)
        register("estherserver:mackerel", 6, ShopCategory.FOOD)
        register("estherserver:squid_catch", 6, ShopCategory.FOOD)
        register("estherserver:anchovy", 4, ShopCategory.FOOD)
        register("estherserver:shrimp", 5, ShopCategory.FOOD)
        register("estherserver:clam", 5, ShopCategory.FOOD)
        // Fish - Advanced
        register("estherserver:salmon_catch", 12, ShopCategory.FOOD)
        register("estherserver:sea_bream", 14, ShopCategory.FOOD)
        register("estherserver:eel", 15, ShopCategory.FOOD)
        register("estherserver:octopus", 13, ShopCategory.FOOD)
        register("estherserver:hairtail", 12, ShopCategory.FOOD)
        register("estherserver:yellowtail", 14, ShopCategory.FOOD)
        // Fish - Rare
        register("estherserver:bluefin_tuna", 40, ShopCategory.FOOD)
        register("estherserver:blowfish", 35, ShopCategory.FOOD)
        register("estherserver:abalone", 38, ShopCategory.FOOD)
        register("estherserver:king_crab", 45, ShopCategory.FOOD)
        register("estherserver:sea_urchin", 42, ShopCategory.FOOD)
        register("estherserver:sturgeon", 50, ShopCategory.FOOD)

        // Seeds - Common
        register("estherserver:rice_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:red_pepper_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:spinach_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:green_onion_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:garlic_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:cabbage_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:soybean_seeds", 2, ShopCategory.SEEDS)
        register("estherserver:sesame_seeds", 2, ShopCategory.SEEDS)
        // Seeds - Advanced
        register("estherserver:ginger_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:perilla_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:lotus_root_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:shiitake_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:bamboo_shoot_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:wasabi_seeds", 5, ShopCategory.SEEDS)
        // Seeds - Rare
        register("estherserver:ginseng_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:truffle_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:saffron_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:matsutake_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:yuzu_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:green_tea_seeds", 12, ShopCategory.SEEDS)
        register("estherserver:special_farmland", 10, ShopCategory.SEEDS)

        // Harvests - Common
        register("estherserver:rice", 4, ShopCategory.FOOD)
        register("estherserver:cooked_rice", 7, ShopCategory.FOOD)
        register("estherserver:red_pepper", 4, ShopCategory.FOOD)
        register("estherserver:spinach", 3, ShopCategory.FOOD)
        register("estherserver:green_onion", 3, ShopCategory.FOOD)
        register("estherserver:garlic", 3, ShopCategory.FOOD)
        register("estherserver:cabbage", 4, ShopCategory.FOOD)
        register("estherserver:soybean", 3, ShopCategory.FOOD)
        register("estherserver:sesame", 3, ShopCategory.FOOD)
        // Harvests - Advanced
        register("estherserver:ginger", 8, ShopCategory.FOOD)
        register("estherserver:perilla", 7, ShopCategory.FOOD)
        register("estherserver:lotus_root", 9, ShopCategory.FOOD)
        register("estherserver:shiitake", 8, ShopCategory.FOOD)
        register("estherserver:bamboo_shoot", 8, ShopCategory.FOOD)
        register("estherserver:wasabi", 10, ShopCategory.FOOD)
        // Harvests - Rare
        register("estherserver:ginseng", 25, ShopCategory.FOOD)
        register("estherserver:truffle", 30, ShopCategory.FOOD)
        register("estherserver:saffron", 35, ShopCategory.FOOD)
        register("estherserver:matsutake", 28, ShopCategory.FOOD)
        register("estherserver:yuzu", 20, ShopCategory.FOOD)
        register("estherserver:green_tea", 22, ShopCategory.FOOD)

        // Minerals - Common (raw)
        register("estherserver:tin_ore_raw", 4, ShopCategory.MINERALS)
        register("estherserver:zinc_ore_raw", 4, ShopCategory.MINERALS)
        register("estherserver:jade_raw", 5, ShopCategory.MINERALS)
        // Minerals - Common (ingot)
        register("estherserver:tin_ingot", 8, ShopCategory.MINERALS)
        register("estherserver:zinc_ingot", 8, ShopCategory.MINERALS)
        // Minerals - Advanced (raw)
        register("estherserver:silver_ore_raw", 10, ShopCategory.MINERALS)
        register("estherserver:ruby_raw", 15, ShopCategory.MINERALS)
        register("estherserver:sapphire_raw", 15, ShopCategory.MINERALS)
        register("estherserver:titanium_ore_raw", 12, ShopCategory.MINERALS)
        // Minerals - Advanced (ingot)
        register("estherserver:silver_ingot", 20, ShopCategory.MINERALS)
        register("estherserver:titanium_ingot", 25, ShopCategory.MINERALS)
        // Minerals - Rare (raw)
        register("estherserver:platinum_ore_raw", 30, ShopCategory.MINERALS)
        register("estherserver:opal_raw", 35, ShopCategory.MINERALS)
        register("estherserver:tanzanite_raw", 40, ShopCategory.MINERALS)
        // Minerals - Rare (ingot)
        register("estherserver:platinum_ingot", 60, ShopCategory.MINERALS)
        // Special
        register("estherserver:obsidian_shard", 25, ShopCategory.MINERALS)

        // Cooking ingredients
        register("estherserver:seaweed", 5, ShopCategory.FOOD)
        register("estherserver:noodles", 5, ShopCategory.FOOD)

        // Cooking dishes - Common
        register("estherserver:spinach_bibimbap", 20, ShopCategory.FOOD)
        register("estherserver:fish_stew", 25, ShopCategory.FOOD)
        register("estherserver:gimbap", 25, ShopCategory.FOOD)
        register("estherserver:kimchi", 20, ShopCategory.FOOD)
        register("estherserver:kimchi_stew", 22, ShopCategory.FOOD)
        register("estherserver:miso_soup", 18, ShopCategory.FOOD)
        register("estherserver:grilled_mackerel", 22, ShopCategory.FOOD)
        register("estherserver:egg_rice", 20, ShopCategory.FOOD)
        // Cooking dishes - Advanced
        register("estherserver:sashimi_platter", 45, ShopCategory.FOOD)
        register("estherserver:eel_rice", 40, ShopCategory.FOOD)
        register("estherserver:dumpling", 35, ShopCategory.FOOD)
        register("estherserver:japchae", 35, ShopCategory.FOOD)
        register("estherserver:ramen", 35, ShopCategory.FOOD)
        register("estherserver:mapo_tofu", 38, ShopCategory.FOOD)
        register("estherserver:seafood_pancake", 40, ShopCategory.FOOD)
        register("estherserver:lotus_salad", 30, ShopCategory.FOOD)
        // Cooking dishes - Rare
        register("estherserver:ginseng_chicken", 80, ShopCategory.FOOD)
        register("estherserver:truffle_risotto", 85, ShopCategory.FOOD)
        register("estherserver:blowfish_sashimi", 90, ShopCategory.FOOD)
        register("estherserver:royal_bibimbap", 75, ShopCategory.FOOD)
        register("estherserver:matsutake_soup", 80, ShopCategory.FOOD)
        register("estherserver:saffron_rice", 70, ShopCategory.FOOD)
        register("estherserver:abalone_porridge", 85, ShopCategory.FOOD)
        register("estherserver:king_crab_stew", 90, ShopCategory.FOOD)
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
        return prices[itemId]?.price
    }

    fun getCategory(itemId: ResourceLocation): ShopCategory? = prices[itemId]?.category
}
