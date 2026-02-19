package com.juyoung.estherserver.economy

import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object ItemPriceRegistry {
    private val prices = mutableMapOf<ResourceLocation, Long>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        registerCustomItemPrices()
        registerVanillaItemPrices()
    }

    private fun registerCustomItemPrices() {
        // Fish
        register("estherserver:test_fish", 5)
        register("estherserver:cooked_test_fish", 8)

        // Seeds (no quality)
        register("estherserver:test_seeds", 1)
        register("estherserver:rice_seeds", 2)
        register("estherserver:red_pepper_seeds", 2)
        register("estherserver:spinach_seeds", 2)

        // Harvests
        register("estherserver:test_harvest", 3)
        register("estherserver:cooked_test_harvest", 6)
        register("estherserver:rice", 4)
        register("estherserver:cooked_rice", 7)
        register("estherserver:red_pepper", 4)
        register("estherserver:spinach", 3)

        // Minerals
        register("estherserver:test_ore_raw", 8)
        register("estherserver:test_ore_ingot", 12)

        // Cooking dishes
        register("estherserver:spinach_bibimbap", 20)
        register("estherserver:fish_stew", 25)
        register("estherserver:gimbap", 25)
        register("estherserver:harvest_bibimbap", 30)
    }

    private fun registerVanillaItemPrices() {
        // Fish
        register("minecraft:cod", 3)
        register("minecraft:salmon", 4)
        register("minecraft:tropical_fish", 8)
        register("minecraft:pufferfish", 6)
        register("minecraft:cooked_cod", 5)
        register("minecraft:cooked_salmon", 6)

        // Crops
        register("minecraft:wheat", 2)
        register("minecraft:potato", 2)
        register("minecraft:carrot", 2)
        register("minecraft:beetroot", 2)
        register("minecraft:melon_slice", 1)
        register("minecraft:pumpkin", 3)
        register("minecraft:sugar_cane", 1)
        register("minecraft:sweet_berries", 1)
        register("minecraft:apple", 2)
        register("minecraft:baked_potato", 4)
        register("minecraft:bread", 4)

        // Minerals
        register("minecraft:coal", 2)
        register("minecraft:raw_iron", 5)
        register("minecraft:raw_gold", 8)
        register("minecraft:raw_copper", 3)
        register("minecraft:iron_ingot", 10)
        register("minecraft:gold_ingot", 15)
        register("minecraft:copper_ingot", 5)
        register("minecraft:diamond", 50)
        register("minecraft:emerald", 30)
        register("minecraft:lapis_lazuli", 5)
        register("minecraft:redstone", 3)
        register("minecraft:quartz", 4)
        register("minecraft:amethyst_shard", 4)
        register("minecraft:netherite_scrap", 100)

        // Mob drops
        register("minecraft:leather", 3)
        register("minecraft:string", 2)
        register("minecraft:feather", 1)
        register("minecraft:bone", 1)
        register("minecraft:gunpowder", 3)
        register("minecraft:ender_pearl", 15)
        register("minecraft:blaze_rod", 10)
        register("minecraft:ghast_tear", 20)
        register("minecraft:slime_ball", 3)
        register("minecraft:phantom_membrane", 8)
        register("minecraft:ink_sac", 2)
        register("minecraft:glow_ink_sac", 5)
    }

    private fun register(item: String, price: Long) {
        prices[ResourceLocation.parse(item)] = price
    }

    fun getBasePrice(itemId: ResourceLocation): Long? = prices[itemId]

    fun getPrice(stack: ItemStack): Long? {
        val itemId = stack.itemHolder.unwrapKey().orElse(null)?.location() ?: return null
        val basePrice = prices[itemId] ?: return null
        val quality = stack.get(ModDataComponents.ITEM_QUALITY.get())
        val multiplier = getQualityMultiplier(quality)
        return (basePrice * multiplier).toLong()
    }

    private fun getQualityMultiplier(quality: ItemQuality?): Double {
        return when (quality) {
            ItemQuality.FINE -> 1.5
            ItemQuality.RARE -> 3.0
            else -> 1.0
        }
    }
}
