package com.juyoung.estherserver.gacha

import com.juyoung.estherserver.pet.PetType
import net.minecraft.world.item.ItemStack
import java.util.function.Supplier

enum class RewardType {
    ITEM,
    PET,
    CURRENCY
}

data class GachaRewardEntry(
    val type: RewardType,
    val weight: Int,
    val displayKey: String,
    val itemSupplier: Supplier<ItemStack>? = null,
    val petType: PetType? = null,
    val currencyAmount: Long = 0
)

class GachaRewardPool(val id: String) {
    private val entries = mutableListOf<GachaRewardEntry>()
    private var totalWeight = 0

    fun addEntry(entry: GachaRewardEntry): GachaRewardPool {
        entries.add(entry)
        totalWeight += entry.weight
        return this
    }

    fun roll(): GachaRewardEntry? {
        if (entries.isEmpty()) return null
        var roll = (0 until totalWeight).random()
        for (entry in entries) {
            roll -= entry.weight
            if (roll < 0) return entry
        }
        return entries.last()
    }

    fun getEntries(): List<GachaRewardEntry> = entries.toList()
}
