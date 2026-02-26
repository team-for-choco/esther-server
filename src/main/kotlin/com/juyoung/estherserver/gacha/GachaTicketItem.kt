package com.juyoung.estherserver.gacha

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class GachaTicketItem(properties: Properties) : Item(properties) {

    override fun use(
        level: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val serverPlayer = player as? ServerPlayer ?: return InteractionResult.PASS
        val stack = player.getItemInHand(usedHand)

        val poolId = GachaRegistry.getPoolId(stack.item)
        if (poolId == null) return InteractionResult.PASS

        val pool = GachaRegistry.getPool(poolId)
        if (pool == null) return InteractionResult.PASS

        val result = GachaHandler.processGacha(serverPlayer, pool)
        if (result) {
            stack.shrink(1)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }
}
