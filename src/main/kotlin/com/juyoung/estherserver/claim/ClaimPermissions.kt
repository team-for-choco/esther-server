package com.juyoung.estherserver.claim

import net.minecraft.nbt.CompoundTag

data class ClaimPermissions(
    val allowBreak: Boolean = false,
    val allowPlace: Boolean = false,
    val allowInteract: Boolean = true
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putBoolean("allowBreak", allowBreak)
        tag.putBoolean("allowPlace", allowPlace)
        tag.putBoolean("allowInteract", allowInteract)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): ClaimPermissions {
            return ClaimPermissions(
                allowBreak = tag.getBoolean("allowBreak"),
                allowPlace = tag.getBoolean("allowPlace"),
                allowInteract = if (tag.contains("allowInteract")) tag.getBoolean("allowInteract") else true
            )
        }
    }
}
