package com.juyoung.estherserver.economy

import com.juyoung.estherserver.EstherServerMod.Companion as Mod
import com.juyoung.estherserver.merchant.ShopCategory
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.registries.DeferredItem

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
        register(Mod.CRUCIAN_CARP, 5, ShopCategory.FOOD)
        register(Mod.SWEETFISH, 5, ShopCategory.FOOD)
        register(Mod.MACKEREL, 7, ShopCategory.FOOD)
        register(Mod.SQUID_CATCH, 7, ShopCategory.FOOD)
        register(Mod.ANCHOVY, 5, ShopCategory.FOOD)
        register(Mod.SHRIMP, 5, ShopCategory.FOOD)
        register(Mod.CLAM, 5, ShopCategory.FOOD)
        // Fish - Advanced
        register(Mod.SALMON_CATCH, 15, ShopCategory.FOOD)
        register(Mod.SEA_BREAM, 17, ShopCategory.FOOD)
        register(Mod.EEL, 18, ShopCategory.FOOD)
        register(Mod.OCTOPUS, 16, ShopCategory.FOOD)
        register(Mod.HAIRTAIL, 15, ShopCategory.FOOD)
        register(Mod.YELLOWTAIL, 17, ShopCategory.FOOD)
        // Fish - Rare
        register(Mod.BLUEFIN_TUNA, 50, ShopCategory.FOOD)
        register(Mod.BLOWFISH, 45, ShopCategory.FOOD)
        register(Mod.ABALONE, 48, ShopCategory.FOOD)
        register(Mod.KING_CRAB, 53, ShopCategory.FOOD)
        register(Mod.SEA_URCHIN, 50, ShopCategory.FOOD)
        register(Mod.STURGEON, 55, ShopCategory.FOOD)

        // Seeds - Common
        register(Mod.RICE_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.RED_PEPPER_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.SPINACH_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.GREEN_ONION_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.GARLIC_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.CABBAGE_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.SOYBEAN_SEEDS, 3, ShopCategory.SEEDS)
        register(Mod.SESAME_SEEDS, 3, ShopCategory.SEEDS)
        // Seeds - Advanced
        register(Mod.GINGER_SEEDS, 8, ShopCategory.SEEDS)
        register(Mod.PERILLA_SEEDS, 8, ShopCategory.SEEDS)
        register(Mod.LOTUS_ROOT_SEEDS, 8, ShopCategory.SEEDS)
        register(Mod.SHIITAKE_SEEDS, 8, ShopCategory.SEEDS)
        register(Mod.BAMBOO_SHOOT_SEEDS, 8, ShopCategory.SEEDS)
        register(Mod.WASABI_SEEDS, 8, ShopCategory.SEEDS)
        // Seeds - Rare
        register(Mod.GINSENG_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.TRUFFLE_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.SAFFRON_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.MATSUTAKE_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.YUZU_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.GREEN_TEA_SEEDS, 15, ShopCategory.SEEDS)
        register(Mod.SPECIAL_FARMLAND_ITEM, 10, ShopCategory.SEEDS)
        // Sprayer / Watering Can
        register(Mod.SPRAYER, 30, ShopCategory.SEEDS)
        register(Mod.WATERING_CAN, 150, ShopCategory.SEEDS)

        // Harvests - Common (all 13, net profit 3)
        register(Mod.RICE, 13, ShopCategory.FOOD)
        register(Mod.COOKED_RICE, 15, ShopCategory.FOOD)
        register(Mod.RED_PEPPER, 13, ShopCategory.FOOD)
        register(Mod.SPINACH, 13, ShopCategory.FOOD)
        register(Mod.GREEN_ONION, 13, ShopCategory.FOOD)
        register(Mod.GARLIC, 13, ShopCategory.FOOD)
        register(Mod.CABBAGE, 13, ShopCategory.FOOD)
        register(Mod.SOYBEAN, 13, ShopCategory.FOOD)
        register(Mod.SESAME, 13, ShopCategory.FOOD)
        // Harvests - Advanced (all 33, net profit 8)
        register(Mod.GINGER, 33, ShopCategory.FOOD)
        register(Mod.PERILLA, 33, ShopCategory.FOOD)
        register(Mod.LOTUS_ROOT, 33, ShopCategory.FOOD)
        register(Mod.SHIITAKE, 33, ShopCategory.FOOD)
        register(Mod.BAMBOO_SHOOT, 33, ShopCategory.FOOD)
        register(Mod.WASABI, 33, ShopCategory.FOOD)
        // Harvests - Rare (all 73, net profit 23)
        register(Mod.GINSENG, 73, ShopCategory.FOOD)
        register(Mod.TRUFFLE, 73, ShopCategory.FOOD)
        register(Mod.SAFFRON, 73, ShopCategory.FOOD)
        register(Mod.MATSUTAKE, 73, ShopCategory.FOOD)
        register(Mod.YUZU, 73, ShopCategory.FOOD)
        register(Mod.GREEN_TEA, 73, ShopCategory.FOOD)

        // Minerals - Common (raw 5, ingot 10)
        register(Mod.TIN_ORE_RAW, 5, ShopCategory.MINERALS)
        register(Mod.ZINC_ORE_RAW, 5, ShopCategory.MINERALS)
        register(Mod.JADE_RAW, 5, ShopCategory.MINERALS)
        // Minerals - Common (ingot)
        register(Mod.TIN_INGOT, 10, ShopCategory.MINERALS)
        register(Mod.ZINC_INGOT, 10, ShopCategory.MINERALS)
        // Minerals - Advanced (raw 13, ingot 26)
        register(Mod.SILVER_ORE_RAW, 13, ShopCategory.MINERALS)
        register(Mod.RUBY_RAW, 13, ShopCategory.MINERALS)
        register(Mod.SAPPHIRE_RAW, 13, ShopCategory.MINERALS)
        register(Mod.TITANIUM_ORE_RAW, 13, ShopCategory.MINERALS)
        // Minerals - Advanced (ingot)
        register(Mod.SILVER_INGOT, 26, ShopCategory.MINERALS)
        register(Mod.TITANIUM_INGOT, 26, ShopCategory.MINERALS)
        // Minerals - Rare (raw 40, ingot 80)
        register(Mod.PLATINUM_ORE_RAW, 40, ShopCategory.MINERALS)
        register(Mod.OPAL_RAW, 40, ShopCategory.MINERALS)
        register(Mod.TANZANITE_RAW, 40, ShopCategory.MINERALS)
        // Minerals - Rare (ingot)
        register(Mod.PLATINUM_INGOT, 80, ShopCategory.MINERALS)
        // Special
        register(Mod.OBSIDIAN_SHARD, 30, ShopCategory.MINERALS)

        // Cooking ingredients
        register(Mod.SEAWEED, 5, ShopCategory.FOOD)
        register(Mod.NOODLES, 5, ShopCategory.FOOD)

        // Cooking dishes - Common (profit 5~10)
        register(Mod.SPINACH_BIBIMBAP, 50, ShopCategory.FOOD)
        register(Mod.FISH_STEW, 36, ShopCategory.FOOD)
        register(Mod.GIMBAP, 44, ShopCategory.FOOD)
        register(Mod.KIMCHI, 60, ShopCategory.FOOD)
        register(Mod.KIMCHI_STEW, 69, ShopCategory.FOOD)
        register(Mod.MISO_SOUP, 45, ShopCategory.FOOD)
        register(Mod.GRILLED_MACKEREL, 40, ShopCategory.FOOD)
        register(Mod.EGG_RICE, 49, ShopCategory.FOOD)
        // Cooking dishes - Advanced (profit 15~20)
        register(Mod.SASHIMI_PLATTER, 85, ShopCategory.FOOD)
        register(Mod.EEL_RICE, 95, ShopCategory.FOOD)
        register(Mod.DUMPLING, 78, ShopCategory.FOOD)
        register(Mod.JAPCHAE, 79, ShopCategory.FOOD)
        register(Mod.RAMEN, 103, ShopCategory.FOOD)
        register(Mod.MAPO_TOFU, 88, ShopCategory.FOOD)
        register(Mod.SEAFOOD_PANCAKE, 80, ShopCategory.FOOD)
        register(Mod.LOTUS_SALAD, 94, ShopCategory.FOOD)
        // Cooking dishes - Rare (profit 23~38)
        register(Mod.GINSENG_CHICKEN, 178, ShopCategory.FOOD)
        register(Mod.TRUFFLE_RISOTTO, 143, ShopCategory.FOOD)
        register(Mod.BLOWFISH_SASHIMI, 160, ShopCategory.FOOD)
        register(Mod.ROYAL_BIBIMBAP, 203, ShopCategory.FOOD)
        register(Mod.MATSUTAKE_SOUP, 161, ShopCategory.FOOD)
        register(Mod.SAFFRON_RICE, 140, ShopCategory.FOOD)
        register(Mod.ABALONE_PORRIDGE, 110, ShopCategory.FOOD)
        register(Mod.KING_CRAB_STEW, 139, ShopCategory.FOOD)
    }

    private fun registerVanillaItemPrices() {
        // Fish
        register("minecraft:cod", 3, ShopCategory.FOOD)
        register("minecraft:salmon", 4, ShopCategory.FOOD)
        register("minecraft:tropical_fish", 8, ShopCategory.FOOD)
        register("minecraft:pufferfish", 6, ShopCategory.FOOD)
        register("minecraft:cooked_cod", 5, ShopCategory.FOOD)
        register("minecraft:cooked_salmon", 6, ShopCategory.FOOD)

        // Seeds
        register("minecraft:wheat_seeds", 2, ShopCategory.SEEDS)
        register("minecraft:pumpkin_seeds", 2, ShopCategory.SEEDS)
        register("minecraft:melon_seeds", 2, ShopCategory.SEEDS)
        register("minecraft:beetroot_seeds", 2, ShopCategory.SEEDS)

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
        register("minecraft:redstone", 250, ShopCategory.MINERALS)
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

    private fun register(item: DeferredItem<*>, price: Long, category: ShopCategory) {
        prices[item.id] = PriceEntry(price, category)
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
