package com.juyoung.estherserver.merchant

import com.juyoung.estherserver.EstherServerMod
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB

object ShopCommand {

    private val CATEGORY_SUGGESTIONS = SuggestionProvider<CommandSourceStack> { _, builder ->
        SharedSuggestionProvider.suggest(
            ShopCategory.entries.map { it.name.lowercase() },
            builder
        )
    }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("shop")
                .then(
                    Commands.literal("summon")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.argument("type", StringArgumentType.word())
                                .suggests(CATEGORY_SUGGESTIONS)
                                .executes { context ->
                                    val typeStr = StringArgumentType.getString(context, "type")
                                    summonMerchant(context.source, typeStr)
                                }
                        )
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

    private fun summonMerchant(source: CommandSourceStack, typeStr: String): Int {
        val category = try {
            ShopCategory.valueOf(typeStr.uppercase())
        } catch (_: IllegalArgumentException) {
            source.sendFailure(
                Component.literal("[상점] 올바른 상인 유형: ${ShopCategory.entries.joinToString(", ") { it.name.lowercase() }}")
            )
            return 0
        }

        val level = source.level
        val pos = source.position

        val entity = MerchantEntity(EstherServerMod.MERCHANT_ENTITY.get(), level)
        entity.setPos(pos.x, pos.y, pos.z)
        entity.merchantType = category
        entity.customName = Component.translatable("entity.estherserver.merchant.${category.name.lowercase()}")
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

        val nearest = merchants.minByOrNull { it.distanceToSqr(pos) } ?: return 0
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
