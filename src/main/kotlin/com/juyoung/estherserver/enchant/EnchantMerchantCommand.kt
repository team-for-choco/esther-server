package com.juyoung.estherserver.enchant

import com.juyoung.estherserver.EstherServerMod
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB

object EnchantMerchantCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("enchantnpc")
                .then(
                    Commands.literal("summon")
                        .requires { it.hasPermission(2) }
                        .executes { context ->
                            summonMerchant(context.source)
                        }
                )
                .then(
                    Commands.literal("remove")
                        .requires { it.hasPermission(2) }
                        .executes { context ->
                            removeNearestMerchant(context.source)
                        }
                )
        )
    }

    private fun summonMerchant(source: CommandSourceStack): Int {
        val level = source.level
        val pos = source.position

        val entity = EnchantMerchantEntity(EstherServerMod.ENCHANT_MERCHANT_ENTITY.get(), level)
        entity.setPos(pos.x, pos.y, pos.z)
        entity.customName = Component.translatable("entity.estherserver.enchant_merchant")
        entity.isCustomNameVisible = true

        level.addFreshEntity(entity)

        source.sendSuccess({
            Component.translatable("message.estherserver.enchant_merchant_summoned")
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

        val merchants = level.getEntitiesOfClass(EnchantMerchantEntity::class.java, searchBox)
        if (merchants.isEmpty()) {
            source.sendFailure(Component.translatable("message.estherserver.enchant_merchant_not_found"))
            return 0
        }

        val nearest = merchants.minByOrNull { it.distanceToSqr(pos) } ?: return 0
        nearest.discard()

        source.sendSuccess({
            Component.translatable("message.estherserver.enchant_merchant_removed")
        }, true)

        return 1
    }
}
