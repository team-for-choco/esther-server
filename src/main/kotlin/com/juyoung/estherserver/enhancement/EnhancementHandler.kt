package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.registries.DeferredItem

object EnhancementHandler {

    const val MAX_LEVEL = 5
    const val EQUIPMENT_BUY_PRICE = 5_000L
    const val ENHANCEMENT_STONE_DROP_RATE = 0.02f // 2%

    data class EnhancementCost(
        val cost: Long,
        val successRate: Double,
        val requiresStone: Boolean
    )

    val ENHANCEMENT_TABLE = mapOf(
        0 to EnhancementCost(1_500L, 1.0, false),
        1 to EnhancementCost(4_500L, 0.8, false),
        2 to EnhancementCost(10_500L, 0.6, false),
        3 to EnhancementCost(22_500L, 0.4, false),
        4 to EnhancementCost(45_000L, 0.15, true)
    )

    val EQUIPMENT_MAP: Map<Profession, DeferredItem<Item>> by lazy {
        mapOf(
            Profession.FISHING to EstherServerMod.SPECIAL_FISHING_ROD,
            Profession.FARMING to EstherServerMod.SPECIAL_HOE,
            Profession.MINING to EstherServerMod.SPECIAL_PICKAXE,
            Profession.COOKING to EstherServerMod.SPECIAL_COOKING_TOOL
        )
    }

    fun getEquipmentLevel(player: ServerPlayer, profession: Profession): Int {
        val equipItem = EQUIPMENT_MAP[profession] ?: return -1
        val stack = findEquipmentInInventory(player, equipItem.get()) ?: return -1
        return stack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
    }

    fun findEquipmentInInventory(player: ServerPlayer, item: Item): ItemStack? {
        for (i in 0 until player.inventory.items.size) {
            val stack = player.inventory.items[i]
            if (!stack.isEmpty && stack.item === item) {
                return stack
            }
        }
        return null
    }

    fun handleBuyEquipment(player: ServerPlayer, professionName: String): Boolean {
        val profession = try {
            Profession.valueOf(professionName.uppercase())
        } catch (_: IllegalArgumentException) {
            return false
        }

        val equipItem = EQUIPMENT_MAP[profession] ?: return false

        if (findEquipmentInInventory(player, equipItem.get()) != null) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.enhance_already_owned")
            )
            return false
        }

        if (!EconomyHandler.removeBalance(player, EQUIPMENT_BUY_PRICE)) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_insufficient")
            )
            return false
        }

        val stack = ItemStack(equipItem.get())
        stack.set(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)

        if (!player.inventory.add(stack)) {
            EconomyHandler.addBalance(player, EQUIPMENT_BUY_PRICE)
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_inventory_full")
            )
            return false
        }

        player.sendSystemMessage(
            Component.translatable(
                "message.estherserver.enhance_buy_success",
                Component.translatable(equipItem.get().descriptionId),
                EQUIPMENT_BUY_PRICE
            )
        )
        return true
    }

    fun handleEnhance(player: ServerPlayer, professionName: String): Boolean {
        val profession = try {
            Profession.valueOf(professionName.uppercase())
        } catch (_: IllegalArgumentException) {
            return false
        }

        val equipItem = EQUIPMENT_MAP[profession] ?: return false
        val stack = findEquipmentInInventory(player, equipItem.get())
        if (stack == null) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.enhance_not_owned")
            )
            return false
        }

        val currentLevel = stack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
        if (currentLevel >= MAX_LEVEL) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.enhance_max_level")
            )
            return false
        }

        val cost = ENHANCEMENT_TABLE[currentLevel] ?: return false

        // Check enhancement stone
        if (cost.requiresStone) {
            if (findEnhancementStone(player) < 0) {
                player.sendSystemMessage(
                    Component.translatable("message.estherserver.enhance_need_stone")
                )
                return false
            }
        }

        // Check balance
        if (!EconomyHandler.removeBalance(player, cost.cost)) {
            player.sendSystemMessage(
                Component.translatable("message.estherserver.shop_insufficient")
            )
            return false
        }

        // Consume enhancement stone (before roll, consumed on attempt)
        if (cost.requiresStone) {
            val stoneSlot = findEnhancementStone(player)
            if (stoneSlot >= 0) {
                player.inventory.getItem(stoneSlot).shrink(1)
            }
        }

        // Roll for success
        val roll = player.random.nextDouble()
        if (roll < cost.successRate) {
            val newLevel = currentLevel + 1
            stack.set(ModDataComponents.ENHANCEMENT_LEVEL.get(), newLevel)
            player.sendSystemMessage(
                Component.translatable(
                    "message.estherserver.enhance_success",
                    Component.translatable(equipItem.get().descriptionId),
                    newLevel
                )
            )
            player.playNotifySound(SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 1.0f)
        } else {
            player.sendSystemMessage(
                Component.translatable(
                    "message.estherserver.enhance_fail",
                    Component.translatable(equipItem.get().descriptionId)
                )
            )
            player.playNotifySound(SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.5f)
        }

        return true
    }

    private fun findEnhancementStone(player: ServerPlayer): Int {
        for (i in 0 until player.inventory.items.size) {
            val stack = player.inventory.items[i]
            if (!stack.isEmpty && stack.item === EstherServerMod.ENHANCEMENT_STONE.get()) {
                return i
            }
        }
        return -1
    }

    fun getGradeTranslationKey(level: Int): String = when {
        level >= 5 -> "quality.estherserver.rare"
        level >= 3 -> "quality.estherserver.fine"
        else -> "quality.estherserver.common"
    }
}
