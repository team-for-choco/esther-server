package com.juyoung.estherserver.wild

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

class WildPortalBlock(properties: Properties) : AbstractPortalBlock(properties) {
    companion object {
        val CODEC: MapCodec<WildPortalBlock> = simpleCodec(::WildPortalBlock)
    }

    override fun codec(): MapCodec<out Block> = CODEC

    override fun getDummyBlock(): Block = EstherServerMod.WILD_PORTAL_DUMMY.get()

    override fun isCorrectDimension(player: ServerPlayer): Boolean =
        player.level().dimension() == Level.OVERWORLD

    override fun getWrongDimensionMessageKey(): String =
        "message.estherserver.wild_portal_wrong_dimension"

    override fun performTeleport(player: ServerPlayer): Boolean =
        WildTeleportHelper.teleportToWild(player)
}
