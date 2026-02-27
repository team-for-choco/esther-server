package com.juyoung.estherserver.cosmetic

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.EquipmentSlot

class CosmeticData(
    val unlockedCosmetics: MutableSet<String> = mutableSetOf(),
    val equipped: MutableMap<EquipmentSlot, String> = mutableMapOf()
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()

        val unlocked = ListTag()
        for (id in unlockedCosmetics) {
            unlocked.add(StringTag.valueOf(id))
        }
        tag.put("Unlocked", unlocked)

        val equip = CompoundTag()
        for ((slot, cosmeticId) in equipped) {
            equip.putString(slot.name, cosmeticId)
        }
        tag.put("Equipped", equip)

        return tag
    }

    companion object {
        private val VALID_SLOTS = setOf(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
        )

        fun fromNBT(tag: CompoundTag): CosmeticData {
            val unlocked = mutableSetOf<String>()
            val list = tag.getList("Unlocked", Tag.TAG_STRING.toInt())
            for (i in 0 until list.size) {
                unlocked.add(list.getString(i))
            }

            val equipped = mutableMapOf<EquipmentSlot, String>()
            if (tag.contains("Equipped")) {
                val equip = tag.getCompound("Equipped")
                for (slot in VALID_SLOTS) {
                    val id = equip.getString(slot.name)
                    if (id.isNotEmpty() && id in unlocked) {
                        equipped[slot] = id
                    }
                }
            }

            return CosmeticData(unlocked, equipped)
        }
    }
}
