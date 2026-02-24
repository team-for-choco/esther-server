package com.juyoung.estherserver.wild

import net.minecraft.nbt.CompoundTag

data class WildReturnData(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var hasData: Boolean = false
) {
    fun toNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putDouble("x", x)
        tag.putDouble("y", y)
        tag.putDouble("z", z)
        tag.putFloat("yaw", yaw)
        tag.putFloat("pitch", pitch)
        tag.putBoolean("hasData", hasData)
        return tag
    }

    companion object {
        fun fromNBT(tag: CompoundTag): WildReturnData {
            return WildReturnData(
                x = tag.getDouble("x"),
                y = tag.getDouble("y"),
                z = tag.getDouble("z"),
                yaw = tag.getFloat("yaw"),
                pitch = tag.getFloat("pitch"),
                hasData = tag.getBoolean("hasData")
            )
        }
    }
}
