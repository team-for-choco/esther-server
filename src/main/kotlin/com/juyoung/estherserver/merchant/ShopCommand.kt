package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.EstherServerMod
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB

object ShopCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("shop")
                .then(
                    Commands.literal("summon")
                        .requires { it.hasPermission(2) }
                        .executes { context -> summonMerchant(context.source) }
                )
                .then(
                    Commands.literal("remove")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("all")
                                .executes { context -> removeAllMerchants(context.source) }
                        )
                        .executes { context -> removeNearestMerchant(context.source) }
                )
        )
    }

    private fun summonMerchant(source: CommandSourceStack): Int {
        val level = source.level
        val pos = source.position

        val entity = MerchantEntity(EstherServerMod.MERCHANT_ENTITY.get(), level)
        entity.setPos(pos.x, pos.y, pos.z)
        entity.customName = Component.translatable("entity.estherserver.merchant")
        entity.isCustomNameVisible = true

        level.addFreshEntity(entity)

        source.sendSuccess({
            Component.translatable("message.estherserver.shop_summoned")
        }, true)

        return 1
    }

    private fun removeNearestMerchant(source: CommandSourceStack): Int {
        val level = source.level
        val pos = source.position
        val searchBox = AABB(
            pos.x - 5, pos.y - 5, pos.z - 5,
            pos.x + 5, pos.y + 5, pos.z + 5
        )

        val merchants = level.getEntitiesOfClass(MerchantEntity::class.java, searchBox)

        if (merchants.isEmpty()) {
            source.sendFailure(Component.translatable("message.estherserver.shop_not_found"))
            return 0
        }

        val nearest = merchants.minByOrNull { it.distanceToSqr(pos) }!!
        nearest.discard()

        source.sendSuccess({
            Component.translatable("message.estherserver.shop_removed")
        }, true)

        return 1
    }

    private fun removeAllMerchants(source: CommandSourceStack): Int {
        val level = source.level
        var count = 0

        level.getEntities(EstherServerMod.MERCHANT_ENTITY.get()) { true }.forEach { entity ->
            entity.discard()
            count++
        }

        if (count == 0) {
            source.sendFailure(Component.translatable("message.estherserver.shop_not_found"))
            return 0
        }

        source.sendSuccess({
            Component.translatable("message.estherserver.shop_removed_all", count)
        }, true)

        return count
    }
}
