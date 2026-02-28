package com.juyoung.estherserver.wild

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block

class ReturnPortalBlock(properties: Properties) : AbstractPortalBlock(properties) {
    companion object {
        val CODEC: MapCodec<ReturnPortalBlock> = simpleCodec(::ReturnPortalBlock)
    }

    override fun codec(): MapCodec<out Block> = CODEC

    override fun getDummyBlock(): Block = EstherServerMod.RETURN_PORTAL_DUMMY.get()

    override fun isCorrectDimension(player: ServerPlayer): Boolean =
        player.level().dimension() == WildDimensionKeys.WILD_LEVEL

    override fun getWrongDimensionMessageKey(): String =
        "message.estherserver.return_portal_wrong_dimension"

    override fun performTeleport(player: ServerPlayer): Boolean =
        WildTeleportHelper.teleportToOverworld(player)
}
