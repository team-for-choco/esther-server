package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.EstherServerMod.Companion as Mod
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Supplier

object GachaRegistry {
    private val pools = mutableMapOf<String, GachaRewardPool>()
    private val itemToPool = mutableMapOf<Item, String>()
    private var initialized = false

    const val POOL_NORMAL = "normal"
    const val POOL_PET_NORMAL = "pet_normal"
    const val POOL_FURNITURE_NORMAL = "furniture_normal"

    fun init() {
        if (initialized) return
        initialized = true

        registerPools()
        registerItemMappings()
    }

    private fun registerPools() {
        // 일반 뽑기권 → 펫 뽑기권 / 가구 뽑기권 / 화폐
        pools[POOL_NORMAL] = GachaRewardPool(POOL_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM,
                weight = 35,
                displayKey = "item.estherserver.pet_draw_ticket_normal",
                itemSupplier = Supplier { ItemStack(Mod.PET_DRAW_TICKET_NORMAL.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM,
                weight = 35,
                displayKey = "item.estherserver.furniture_draw_ticket_normal",
                itemSupplier = Supplier { ItemStack(Mod.FURNITURE_DRAW_TICKET_NORMAL.get()) }
            ))
            .addEntry(GachaRewardEntry(
                type = RewardType.CURRENCY,
                weight = 30,
                displayKey = "message.estherserver.gacha_currency_bonus",
                currencyAmount = 300
            ))

        // 펫 뽑기권 → 펫 토큰 아이템
        pools[POOL_PET_NORMAL] = GachaRewardPool(POOL_PET_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM,
                weight = 100,
                displayKey = "item.estherserver.pet_token_cat_common",
                itemSupplier = Supplier { ItemStack(Mod.PET_TOKEN_CAT_COMMON.get()) }
            ))

        // 가구 뽑기권 → 일반 등급 가구
        pools[POOL_FURNITURE_NORMAL] = GachaRewardPool(POOL_FURNITURE_NORMAL)
            .addEntry(GachaRewardEntry(
                type = RewardType.ITEM,
                weight = 100,
                displayKey = "block.estherserver.cat_sofa",
                itemSupplier = Supplier { ItemStack(Mod.CAT_SOFA_ITEM.get()) }
            ))
    }

    private fun registerItemMappings() {
        itemToPool[Mod.DRAW_TICKET_NORMAL.get()] = POOL_NORMAL
        itemToPool[Mod.PET_DRAW_TICKET_NORMAL.get()] = POOL_PET_NORMAL
        itemToPool[Mod.FURNITURE_DRAW_TICKET_NORMAL.get()] = POOL_FURNITURE_NORMAL
    }

    fun getPoolId(item: Item): String? = itemToPool[item]

    fun getPool(poolId: String): GachaRewardPool? = pools[poolId]
}
