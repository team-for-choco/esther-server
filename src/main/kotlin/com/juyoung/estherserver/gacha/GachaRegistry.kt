package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.EstherServerMod.Companion as Mod
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.Supplier

object GachaRegistry {
    private val pools = mutableMapOf<String, GachaRewardPool>()
    private val itemToPool = mutableMapOf<Item, String>()
    private var initialized = false

    const val POOL_NORMAL = "normal"
    const val POOL_PET_NORMAL = "pet_normal"
    const val POOL_FURNITURE_NORMAL = "furniture_normal"
    const val POOL_COSMETIC_NORMAL = "cosmetic_normal"

    fun init() {
        if (initialized) return
        initialized = true

        registerPools()
        registerItemMappings()
    }

    private fun registerPools() {
        // ─── 일반 뽑기권 (합계 1000) ───
        // 화폐 + 만년 수프: 70%
        pools[POOL_NORMAL] = GachaRewardPool(POOL_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.CURRENCY, weight = 170,
                displayKey = "message.estherserver.gacha_currency_100",
                displayItemId = "minecraft:gold_ingot",
                currencyAmount = 100
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.CURRENCY, weight = 130,
                displayKey = "message.estherserver.gacha_currency_300",
                displayItemId = "minecraft:gold_ingot",
                currencyAmount = 300
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.CURRENCY, weight = 50,
                displayKey = "message.estherserver.gacha_currency_500",
                displayItemId = "minecraft:gold_ingot",
                currencyAmount = 500
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 190,
                displayKey = "item.estherserver.hunters_pot",
                displayItemId = "estherserver:hunters_pot",
                itemSupplier = Supplier { ItemStack(Mod.HUNTERS_POT.get(), 10) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.hunters_pot",
                displayItemId = "estherserver:hunters_pot",
                itemSupplier = Supplier { ItemStack(Mod.HUNTERS_POT.get(), 30) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 40,
                displayKey = "item.estherserver.hunters_pot",
                displayItemId = "estherserver:hunters_pot",
                itemSupplier = Supplier { ItemStack(Mod.HUNTERS_POT.get(), 50) }
            ))
            // 기타: 30%
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 30,
                displayKey = "item.estherserver.pet_draw_ticket_normal",
                displayItemId = "estherserver:pet_draw_ticket_normal",
                itemSupplier = Supplier { ItemStack(Mod.PET_DRAW_TICKET_NORMAL.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 30,
                displayKey = "item.estherserver.furniture_draw_ticket_normal",
                displayItemId = "estherserver:furniture_draw_ticket_normal",
                itemSupplier = Supplier { ItemStack(Mod.FURNITURE_DRAW_TICKET_NORMAL.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 45,
                displayKey = "item.minecraft.coal",
                displayItemId = "minecraft:coal",
                itemSupplier = Supplier { ItemStack(Items.COAL, 15) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 40,
                displayKey = "item.minecraft.raw_copper",
                displayItemId = "minecraft:raw_copper",
                itemSupplier = Supplier { ItemStack(Items.RAW_COPPER, 15) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 35,
                displayKey = "item.minecraft.raw_iron",
                displayItemId = "minecraft:raw_iron",
                itemSupplier = Supplier { ItemStack(Items.RAW_IRON, 5) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 25,
                displayKey = "item.minecraft.raw_gold",
                displayItemId = "minecraft:raw_gold",
                itemSupplier = Supplier { ItemStack(Items.RAW_GOLD, 3) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 25,
                displayKey = "item.minecraft.lapis_lazuli",
                displayItemId = "minecraft:lapis_lazuli",
                itemSupplier = Supplier { ItemStack(Items.LAPIS_LAZULI, 3) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 15,
                displayKey = "item.minecraft.diamond",
                displayItemId = "minecraft:diamond",
                itemSupplier = Supplier { ItemStack(Items.DIAMOND, 1) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 15,
                displayKey = "item.minecraft.emerald",
                displayItemId = "minecraft:emerald",
                itemSupplier = Supplier { ItemStack(Items.EMERALD, 1) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 5,
                displayKey = "item.estherserver.enhancement_stone",
                displayItemId = "estherserver:enhancement_stone",
                itemSupplier = Supplier { ItemStack(Mod.ENHANCEMENT_STONE.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 35,
                displayKey = "item.estherserver.land_deed",
                displayItemId = "estherserver:land_deed",
                itemSupplier = Supplier { ItemStack(Mod.LAND_DEED.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 20,
                displayKey = "item.estherserver.inventory_save_ticket",
                displayItemId = "estherserver:inventory_save_ticket",
                itemSupplier = Supplier { ItemStack(Mod.INVENTORY_SAVE_TICKET.get()) }
            ))

        // ─── 펫 뽑기권 ───
        pools[POOL_PET_NORMAL] = GachaRewardPool(POOL_PET_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.pet_token_cat_common",
                displayItemId = "estherserver:pet_token_cat_common",
                itemSupplier = Supplier { ItemStack(Mod.PET_TOKEN_CAT_COMMON.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.pet_token_dog_common",
                displayItemId = "estherserver:pet_token_dog_common",
                itemSupplier = Supplier { ItemStack(Mod.PET_TOKEN_DOG_COMMON.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.pet_token_rabbit_common",
                displayItemId = "estherserver:pet_token_rabbit_common",
                itemSupplier = Supplier { ItemStack(Mod.PET_TOKEN_RABBIT_COMMON.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.pet_token_fox_common",
                displayItemId = "estherserver:pet_token_fox_common",
                itemSupplier = Supplier { ItemStack(Mod.PET_TOKEN_FOX_COMMON.get()) }
            ))

        // ─── 가구 뽑기권 ───
        pools[POOL_FURNITURE_NORMAL] = GachaRewardPool(POOL_FURNITURE_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "block.estherserver.cat_sofa",
                displayItemId = "estherserver:cat_sofa",
                itemSupplier = Supplier { ItemStack(Mod.CAT_SOFA_ITEM.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "block.estherserver.dog_sofa",
                displayItemId = "estherserver:dog_sofa",
                itemSupplier = Supplier { ItemStack(Mod.DOG_SOFA_ITEM.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "block.estherserver.rabbit_sofa",
                displayItemId = "estherserver:rabbit_sofa",
                itemSupplier = Supplier { ItemStack(Mod.RABBIT_SOFA_ITEM.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "block.estherserver.fox_sofa",
                displayItemId = "estherserver:fox_sofa",
                itemSupplier = Supplier { ItemStack(Mod.FOX_SOFA_ITEM.get()) }
            ))

        // ─── 치장 뽑기권 ───
        pools[POOL_COSMETIC_NORMAL] = GachaRewardPool(POOL_COSMETIC_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.cosmetic_token_cat_ears",
                displayItemId = "estherserver:cosmetic_token_cat_ears",
                itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_CAT_EARS.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.cosmetic_token_cat_hoodie",
                displayItemId = "estherserver:cosmetic_token_cat_hoodie",
                itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_CAT_HOODIE.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.cosmetic_token_cat_pants",
                displayItemId = "estherserver:cosmetic_token_cat_pants",
                itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_CAT_PANTS.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM, weight = 100,
                displayKey = "item.estherserver.cosmetic_token_cat_paws",
                displayItemId = "estherserver:cosmetic_token_cat_paws",
                itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_CAT_PAWS.get()) }
            ))
            // Dog cosmetics
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_dog_ears", displayItemId = "estherserver:cosmetic_token_dog_ears", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_DOG_EARS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_dog_hoodie", displayItemId = "estherserver:cosmetic_token_dog_hoodie", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_DOG_HOODIE.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_dog_pants", displayItemId = "estherserver:cosmetic_token_dog_pants", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_DOG_PANTS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_dog_paws", displayItemId = "estherserver:cosmetic_token_dog_paws", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_DOG_PAWS.get()) }))
            // Rabbit cosmetics
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_rabbit_ears", displayItemId = "estherserver:cosmetic_token_rabbit_ears", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_RABBIT_EARS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_rabbit_hoodie", displayItemId = "estherserver:cosmetic_token_rabbit_hoodie", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_RABBIT_HOODIE.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_rabbit_pants", displayItemId = "estherserver:cosmetic_token_rabbit_pants", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_RABBIT_PANTS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_rabbit_paws", displayItemId = "estherserver:cosmetic_token_rabbit_paws", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_RABBIT_PAWS.get()) }))
            // Fox cosmetics
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_fox_ears", displayItemId = "estherserver:cosmetic_token_fox_ears", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_FOX_EARS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_fox_hoodie", displayItemId = "estherserver:cosmetic_token_fox_hoodie", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_FOX_HOODIE.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_fox_pants", displayItemId = "estherserver:cosmetic_token_fox_pants", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_FOX_PANTS.get()) }))
            .addEntry(GachaRewardEntry(type = RewardType.ITEM, weight = 100, displayKey = "item.estherserver.cosmetic_token_fox_paws", displayItemId = "estherserver:cosmetic_token_fox_paws", itemSupplier = Supplier { ItemStack(Mod.COSMETIC_TOKEN_FOX_PAWS.get()) }))
    }

    private fun registerItemMappings() {
        itemToPool[Mod.DRAW_TICKET_NORMAL.get()] = POOL_NORMAL
        itemToPool[Mod.PET_DRAW_TICKET_NORMAL.get()] = POOL_PET_NORMAL
        itemToPool[Mod.FURNITURE_DRAW_TICKET_NORMAL.get()] = POOL_FURNITURE_NORMAL
        itemToPool[Mod.COSMETIC_DRAW_TICKET_NORMAL.get()] = POOL_COSMETIC_NORMAL
    }

    fun getPoolId(item: Item): String? = itemToPool[item]

    fun getPool(poolId: String): GachaRewardPool? = pools[poolId]
}
