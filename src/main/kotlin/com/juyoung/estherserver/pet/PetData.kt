package com.juyoung.estherserver.pet

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag

/**
 * 플레이어별 펫 보유 데이터.
 */
class PetData(
    val ownedPets: MutableList<PetType> = mutableListOf(PetType.CAT_COMMON),
    var summonedPet: PetType? = null,
    var summonedEntityId: Int = -1
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        val list = ListTag()
        for (pet in ownedPets) {
            list.add(StringTag.valueOf(pet.name))
        }
        tag.put("OwnedPets", list)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): PetData {
            val owned = mutableListOf<PetType>()
            val list = tag.getList("OwnedPets", Tag.TAG_STRING.toInt())
            for (i in 0 until list.size) {
                PetType.fromName(list.getString(i))?.let { owned.add(it) }
            }
            if (owned.isEmpty()) {
                owned.add(PetType.CAT_COMMON)
            }
            return PetData(owned)
        }
    }
}
