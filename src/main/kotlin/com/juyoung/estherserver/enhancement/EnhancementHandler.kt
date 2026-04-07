package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.inventory.ModInventory
import com.juyoung.estherserver.inventory.ProfessionInventoryHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomModelData
import net.neoforged.neoforge.registries.DeferredItem

object EnhancementHandler {

    const val MAX_LEVEL = 5
    const val EQUIPMENT_BUY_PRICE = 1_000L
    const val ENHANCEMENT_STONE_DROP_RATE = 0.02f // 2%

    data class EnhancementCost(
        val cost: Long,
        val successRate: Double,
        val stoneCount: Int = 0
    ) {
        val requiresStone get() = stoneCount > 0
    }

    val ENHANCEMENT_TABLE = mapOf(
        0 to EnhancementCost(500L, 1.0),
        1 to EnhancementCost(1_500L, 0.8),
        2 to EnhancementCost(4_500L, 0.6),
        3 to EnhancementCost(22_500L, 0.4),
        4 to EnhancementCost(31_500L, 0.15, stoneCount = 5)
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
        // 일반 인벤토리 + 오프핸드 검색
        player.inventory.items.firstOrNull { !it.isEmpty && it.item === item }?.let { return it }
        player.inventory.offhand.firstOrNull { !it.isEmpty && it.item === item }?.let { return it }

        // 전문 보관함 검색 (도구 슬롯 + 일반 슬롯)
        val profInvData = player.getData(ModInventory.PROFESSION_INVENTORY.get())
        for (profession in Profession.entries) {
            val toolSlot = profInvData.getTool(profession)
            if (!toolSlot.isEmpty && toolSlot.item === item) return toolSlot
            profInvData.getItems(profession).firstOrNull { !it.isEmpty && it.item === item }?.let { return it }
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
        syncCustomModelData(stack, 0)

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
            val stoneCount = countEnhancementStones(player)
            if (stoneCount < cost.stoneCount) {
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

        // Consume enhancement stones (before roll, consumed on attempt)
        if (cost.requiresStone) {
            consumeEnhancementStones(player, cost.stoneCount)
        }

        // Roll for success
        val roll = player.random.nextDouble()
        if (roll < cost.successRate) {
            val newLevel = currentLevel + 1
            stack.set(ModDataComponents.ENHANCEMENT_LEVEL.get(), newLevel)
            syncCustomModelData(stack, newLevel)
            ProfessionInventoryHandler.syncToClient(player)
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

    private fun countEnhancementStones(player: ServerPlayer): Int {
        val stoneItem = EstherServerMod.ENHANCEMENT_STONE.get()
        return (player.inventory.items + player.inventory.offhand).sumOf { stack ->
            if (!stack.isEmpty && stack.item === stoneItem) stack.count else 0
        }
    }

    private fun consumeEnhancementStones(player: ServerPlayer, count: Int) {
        val stoneItem = EstherServerMod.ENHANCEMENT_STONE.get()
        var remaining = count
        for (stack in player.inventory.items + player.inventory.offhand) {
            if (remaining <= 0) break
            if (!stack.isEmpty && stack.item === stoneItem) {
                val consume = minOf(remaining, stack.count)
                stack.shrink(consume)
                remaining -= consume
            }
        }
    }

    private fun syncCustomModelData(stack: ItemStack, enhancementLevel: Int) {
        val gradeFloat = when {
            enhancementLevel >= 5 -> 2.0f  // rare
            enhancementLevel >= 3 -> 1.0f  // fine
            else -> 0.0f                   // common
        }
        stack.set(
            DataComponents.CUSTOM_MODEL_DATA,
            CustomModelData(listOf(gradeFloat), listOf(), listOf(), listOf())
        )
    }

    fun getGradeTranslationKey(level: Int): String = when {
        level >= 5 -> "enhancement.estherserver.grade.rare"
        level >= 3 -> "enhancement.estherserver.grade.fine"
        else -> "enhancement.estherserver.grade.common"
    }
}
