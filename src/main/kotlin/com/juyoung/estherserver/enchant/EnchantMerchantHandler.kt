package com.juyoung.estherserver.enchant

import com.juyoung.estherserver.economy.EconomyHandler
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.neoforged.neoforge.network.PacketDistributor
import java.util.UUID

object EnchantMerchantHandler {

    const val OVERWRITE_COST = 500L
    const val CHOOSE_COST = 1500L
    const val UNLOCK_COST = 5000L
    const val MAX_SLOTS = 4

    // Pending CHOOSE offers: player UUID → list of (holder, level) to apply
    private val pendingOffers = mutableMapOf<UUID, List<Pair<Holder<Enchantment>, Int>>>()

    fun handleRequest(player: ServerPlayer, mode: String) {
        val item = player.mainHandItem
        if (item.isEmpty) {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_no_item"))
            return
        }

        when (mode) {
            "OVERWRITE" -> {
                val slotCount = (item.get(DataComponents.ENCHANTMENTS) ?: ItemEnchantments.EMPTY).size()
                if (slotCount == 0) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.enchant_no_slots"))
                    return
                }
                if (!EconomyHandler.removeBalance(player, OVERWRITE_COST)) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.shop_insufficient"))
                    return
                }
                val picks = pickDistinctEnchantments(player, slotCount)
                applyAll(player, picks)
                player.sendSystemMessage(Component.translatable("message.estherserver.enchant_success"))
            }

            "CHOOSE" -> {
                val slotCount = (item.get(DataComponents.ENCHANTMENTS) ?: ItemEnchantments.EMPTY).size()
                if (slotCount == 0) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.enchant_no_slots"))
                    return
                }
                if (!EconomyHandler.removeBalance(player, CHOOSE_COST)) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.shop_insufficient"))
                    return
                }
                val picks = pickDistinctEnchantments(player, slotCount)
                pendingOffers[player.uuid] = picks
                val previewList = picks.map { (holder, level) ->
                    Pair(holder.unwrapKey().map { it.location().toString() }.orElse("?"), level)
                }
                PacketDistributor.sendToPlayer(player, EnchantPreviewPayload(previewList))
            }

            "UNLOCK" -> {
                val existing = item.get(DataComponents.ENCHANTMENTS) ?: ItemEnchantments.EMPTY
                if (existing.size() >= MAX_SLOTS) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.enchant_max_slots", MAX_SLOTS))
                    return
                }
                if (!EconomyHandler.removeBalance(player, UNLOCK_COST)) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.shop_insufficient"))
                    return
                }
                val existingTypes = existing.entrySet().map { it.key }.toSet()
                val newPick = pickEnchantmentExcluding(player, existingTypes)
                if (newPick == null) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.enchant_failed"))
                    EconomyHandler.addBalance(player, UNLOCK_COST) // 환불
                    return
                }
                val mutable = ItemEnchantments.Mutable(existing)
                mutable.set(newPick.first, newPick.second)
                item.set(DataComponents.ENCHANTMENTS, mutable.toImmutable())
                player.sendSystemMessage(Component.translatable("message.estherserver.enchant_slot_unlocked"))
            }
        }
    }

    fun handleConfirm(player: ServerPlayer, accept: Boolean) {
        val offer = pendingOffers.remove(player.uuid) ?: return

        if (!accept) {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_declined"))
            return
        }

        val item = player.mainHandItem
        if (item.isEmpty) {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_no_item"))
            return
        }

        applyAll(player, offer)
        player.sendSystemMessage(Component.translatable("message.estherserver.enchant_success"))
    }

    fun clearPendingOffer(playerUuid: UUID) {
        pendingOffers.remove(playerUuid)
    }

    private fun applyAll(player: ServerPlayer, picks: List<Pair<Holder<Enchantment>, Int>>) {
        val item = player.mainHandItem
        val mutable = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
        for ((holder, level) in picks) {
            mutable.set(holder, level)
        }
        item.set(DataComponents.ENCHANTMENTS, mutable.toImmutable())
    }

    // 현재 슬롯 수만큼 겹치지 않는 인챈트를 랜덤 선택
    private fun pickDistinctEnchantments(player: ServerPlayer, count: Int): List<Pair<Holder<Enchantment>, Int>> {
        val registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val allHolders = registry.listElements().toList()
        val picked = mutableSetOf<Int>()
        val result = mutableListOf<Pair<Holder<Enchantment>, Int>>()
        val max = count.coerceAtMost(allHolders.size)
        while (result.size < max) {
            val idx = player.random.nextInt(allHolders.size)
            if (picked.add(idx)) {
                val holder = allHolders[idx]
                val maxLevel = holder.value().maxLevel
                val level = player.random.nextInt(maxLevel) + 1
                result.add(Pair(holder, level))
            }
        }
        return result
    }

    // 기존 타입을 제외하고 인챈트 1개 선택 (UNLOCK용)
    private fun pickEnchantmentExcluding(
        player: ServerPlayer,
        exclude: Set<Holder<Enchantment>>
    ): Pair<Holder<Enchantment>, Int>? {
        val registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val excludeKeys = exclude.mapNotNull { it.unwrapKey().orElse(null) }.toSet()
        val candidates = registry.listElements().toList().filter { holder ->
            holder.unwrapKey().orElse(null) !in excludeKeys
        }
        if (candidates.isEmpty()) return null
        val holder = candidates[player.random.nextInt(candidates.size)]
        val maxLevel = holder.value().maxLevel
        val level = player.random.nextInt(maxLevel) + 1
        return Pair(holder, level)
    }
}
