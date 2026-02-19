package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.economy.ItemPriceRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

data class ShopEntry(
    val itemId: ResourceLocation,
    val buyPrice: Long,
    val category: ShopCategory
)

enum class ShopCategory(val translationKey: String) {
    SEEDS("gui.estherserver.shop.category.seeds"),
    FOOD("gui.estherserver.shop.category.food"),
    MINERALS("gui.estherserver.shop.category.minerals"),
    SPECIAL("gui.estherserver.shop.category.special"),
    BLACKSMITH("gui.estherserver.shop.category.blacksmith")
}

object ShopBuyRegistry {
    private val entries = mutableListOf<ShopEntry>()
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        registerSeeds()
        registerFood()
        registerMinerals()
        registerSpecial()
    }

    private fun registerSeeds() {
        register("minecraft:wheat_seeds", 5, ShopCategory.SEEDS)
        register("minecraft:pumpkin_seeds", 5, ShopCategory.SEEDS)
        register("minecraft:melon_seeds", 5, ShopCategory.SEEDS)
        register("minecraft:beetroot_seeds", 5, ShopCategory.SEEDS)
        register("estherserver:test_seeds", 6, ShopCategory.SEEDS)
        register("estherserver:rice_seeds", 8, ShopCategory.SEEDS)
        register("estherserver:red_pepper_seeds", 8, ShopCategory.SEEDS)
        register("estherserver:spinach_seeds", 8, ShopCategory.SEEDS)
    }

    private fun registerFood() {
        register("minecraft:carrot", 6, ShopCategory.FOOD)
        register("minecraft:potato", 6, ShopCategory.FOOD)
        register("minecraft:apple", 8, ShopCategory.FOOD)
        register("minecraft:sweet_berries", 8, ShopCategory.FOOD)
        register("minecraft:bread", 10, ShopCategory.FOOD)
        register("minecraft:sugar_cane", 10, ShopCategory.FOOD)
        register("minecraft:cooked_cod", 15, ShopCategory.FOOD)
    }

    private fun registerMinerals() {
        register("minecraft:coal", 5, ShopCategory.MINERALS)
        register("minecraft:raw_copper", 8, ShopCategory.MINERALS)
        register("minecraft:redstone", 8, ShopCategory.MINERALS)
        register("minecraft:lapis_lazuli", 10, ShopCategory.MINERALS)
        register("minecraft:raw_iron", 12, ShopCategory.MINERALS)
    }

    private fun registerSpecial() {
        register("estherserver:land_deed", 500, ShopCategory.SPECIAL)
    }

    private fun register(item: String, price: Long, category: ShopCategory) {
        entries.add(ShopEntry(ResourceLocation.parse(item), price, category))
    }

    private fun hasInventorySpace(player: ServerPlayer, item: net.minecraft.world.item.Item, quantity: Int): Boolean {
        var space = 0
        val maxStack = item.defaultMaxStackSize
        for (i in 0 until player.inventory.items.size) {
            val slot = player.inventory.items[i]
            if (slot.isEmpty) {
                space += maxStack
            } else if (slot.item === item && slot.count < maxStack) {
                space += maxStack - slot.count
            }
            if (space >= quantity) return true
        }
        return false
    }

    fun getAllEntries(): List<ShopEntry> = entries.toList()

    fun getEntry(itemId: ResourceLocation): ShopEntry? =
        entries.find { it.itemId == itemId }

    fun handleBuy(player: ServerPlayer, itemIdStr: String, quantity: Int): Boolean {
        val itemId = ResourceLocation.tryParse(itemIdStr) ?: return false
        val entry = getEntry(itemId) ?: return false

        val totalCost = entry.buyPrice * quantity

        val item = BuiltInRegistries.ITEM.getValue(itemId)
        if (item === Items.AIR) return false

        // Check inventory space before purchase
        if (!hasInventorySpace(player, item, quantity)) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_inventory_full")
            )
            return false
        }

        if (!EconomyHandler.removeBalance(player, totalCost)) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_insufficient")
            )
            return false
        }

        // Give items respecting max stack size
        var remaining = quantity
        while (remaining > 0) {
            val stackSize = remaining.coerceAtMost(item.defaultMaxStackSize)
            val stack = ItemStack(item, stackSize)
            player.inventory.add(stack)
            remaining -= stackSize
        }

        player.sendSystemMessage(
            Component.translatable(
                "message.estherserver.shop_buy_success",
                quantity,
                Component.translatable(item.descriptionId),
                totalCost
            )
        )

        return true
    }

    fun handleSell(player: ServerPlayer, slotIndex: Int, quantity: Int, merchantType: ShopCategory): Boolean {
        // Validate slot index (0..35 = hotbar + main inventory)
        if (slotIndex < 0 || slotIndex > 35) return false

        val stack = player.inventory.getItem(slotIndex)
        if (stack.isEmpty) return false

        val itemId = stack.itemHolder.unwrapKey().orElse(null)?.location() ?: return false

        // Check category matches merchant type
        val category = ItemPriceRegistry.getCategory(itemId)
        if (category == null) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_sell_no_price")
            )
            return false
        }
        if (category != merchantType) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_sell_wrong_merchant")
            )
            return false
        }

        // Get price per item (with quality multiplier)
        val pricePerItem = ItemPriceRegistry.getPrice(stack) ?: return false

        // Validate quantity
        val sellCount = quantity.coerceAtMost(stack.count)
        if (sellCount <= 0) return false

        val totalPrice = pricePerItem * sellCount
        val itemName = Component.translatable(stack.item.descriptionId)

        // Execute sale
        stack.shrink(sellCount)
        EconomyHandler.addBalance(player, totalPrice)

        player.sendSystemMessage(
            Component.translatable(
                "message.estherserver.shop_sell_success",
                sellCount,
                itemName,
                totalPrice
            )
        )

        return true
    }
}
