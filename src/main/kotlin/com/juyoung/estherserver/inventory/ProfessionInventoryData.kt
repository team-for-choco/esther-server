package com.juyoung.estherserver.inventory

import com.juyoung.estherserver.profession.Profession
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack

class ProfessionInventoryData(
    private val inventories: MutableMap<Profession, MutableList<ItemStack>> = mutableMapOf()
) {
    companion object {
        const val MAX_SLOTS = 25

        fun fromNBT(tag: CompoundTag, registries: HolderLookup.Provider): ProfessionInventoryData {
            val data = ProfessionInventoryData()
            for (profession in Profession.entries) {
                if (tag.contains(profession.name)) {
                    val listTag = tag.getList(profession.name, 10)
                    val items = mutableListOf<ItemStack>()
                    for (i in 0 until listTag.size) {
                        val itemTag = listTag.getCompound(i)
                        val slotIndex = itemTag.getInt("Slot")
                        val stack = ItemStack.parse(registries, itemTag)
                        if (stack.isPresent) {
                            // Pad with empty stacks to maintain slot positions
                            while (items.size <= slotIndex) {
                                items.add(ItemStack.EMPTY)
                            }
                            items[slotIndex] = stack.get()
                        }
                    }
                    data.inventories[profession] = items
                }
            }
            return data
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ProfessionInventoryData> =
            object : StreamCodec<RegistryFriendlyByteBuf, ProfessionInventoryData> {
                override fun decode(buf: RegistryFriendlyByteBuf): ProfessionInventoryData {
                    val data = ProfessionInventoryData()
                    val profCount = buf.readVarInt()
                    repeat(profCount) {
                        val ordinal = buf.readVarInt()
                        val slotCount = buf.readVarInt()
                        val items = mutableListOf<ItemStack>()
                        repeat(slotCount) {
                            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf))
                        }
                        if (ordinal in Profession.entries.indices) {
                            data.inventories[Profession.entries[ordinal]] = items
                        }
                    }
                    return data
                }

                override fun encode(buf: RegistryFriendlyByteBuf, value: ProfessionInventoryData) {
                    buf.writeVarInt(Profession.entries.size)
                    for (profession in Profession.entries) {
                        buf.writeVarInt(profession.ordinal)
                        val items = value.getItems(profession)
                        buf.writeVarInt(items.size)
                        for (stack in items) {
                            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack)
                        }
                    }
                }
            }
    }

    fun toNBT(registries: HolderLookup.Provider): CompoundTag {
        val tag = CompoundTag()
        for ((profession, items) in inventories) {
            val listTag = ListTag()
            for ((index, stack) in items.withIndex()) {
                if (!stack.isEmpty) {
                    val itemTag = stack.save(registries) as CompoundTag
                    itemTag.putInt("Slot", index)
                    listTag.add(itemTag)
                }
            }
            tag.put(profession.name, listTag)
        }
        return tag
    }

    fun getItems(profession: Profession): List<ItemStack> =
        inventories.getOrDefault(profession, mutableListOf())

    fun getItem(profession: Profession, slot: Int): ItemStack {
        val items = inventories[profession] ?: return ItemStack.EMPTY
        return if (slot in items.indices) items[slot] else ItemStack.EMPTY
    }

    fun setItem(profession: Profession, slot: Int, stack: ItemStack) {
        val items = inventories.getOrPut(profession) { mutableListOf() }
        while (items.size <= slot) {
            items.add(ItemStack.EMPTY)
        }
        items[slot] = stack
    }

    fun getUsedSlotCount(profession: Profession): Int =
        inventories[profession]?.count { !it.isEmpty } ?: 0

    /** Try to add an item to the profession inventory. Returns true if successful. */
    fun tryAddItem(profession: Profession, stack: ItemStack, availableSlots: Int): Boolean {
        if (availableSlots <= 0) return false
        val items = inventories.getOrPut(profession) { mutableListOf() }
        while (items.size < availableSlots) {
            items.add(ItemStack.EMPTY)
        }

        // First try to merge with existing stacks
        for (i in 0 until availableSlots.coerceAtMost(items.size)) {
            val existing = items[i]
            if (!existing.isEmpty && ItemStack.isSameItemSameComponents(existing, stack)) {
                val space = existing.maxStackSize - existing.count
                if (space > 0) {
                    val toAdd = stack.count.coerceAtMost(space)
                    existing.grow(toAdd)
                    stack.shrink(toAdd)
                    if (stack.isEmpty) return true
                }
            }
        }

        // Then try to fill empty slots
        for (i in 0 until availableSlots.coerceAtMost(items.size)) {
            if (items[i].isEmpty) {
                items[i] = stack.copy()
                stack.count = 0
                return true
            }
        }

        return false
    }
}
