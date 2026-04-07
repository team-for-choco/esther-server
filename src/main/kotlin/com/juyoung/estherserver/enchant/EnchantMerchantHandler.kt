package com.juyoung.estherserver.enchant

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.economy.EconomyHandler
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.neoforged.neoforge.network.PacketDistributor
import java.util.UUID

object EnchantMerchantHandler {

    const val OVERWRITE_COST = 500L
    const val CHOOSE_COST = 1500L

    // Pending choose offers: player UUID → (enchantId, level, holder)
    private val pendingOffers = mutableMapOf<UUID, Triple<ResourceLocation, Int, Holder<Enchantment>>>()

    fun handleRequest(player: ServerPlayer, mode: String) {
        val item = player.mainHandItem
        if (item.isEmpty) {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_no_item"))
            return
        }

        val pick = pickRandomEnchantment(player) ?: run {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_failed"))
            return
        }
        val (holder, level) = pick
        val enchantId = holder.unwrapKey().map { it.location() }.orElse(null) ?: run {
            player.sendSystemMessage(Component.translatable("message.estherserver.enchant_failed"))
            return
        }

        when (mode) {
            "OVERWRITE" -> {
                if (!EconomyHandler.removeBalance(player, OVERWRITE_COST)) {
                    player.sendSystemMessage(Component.translatable("message.estherserver.shop_insufficient"))
                    return
                }
                applyEnchantment(player, holder, level)
                player.sendSystemMessage(
                    Component.translatable("message.estherserver.enchant_success")
                )
            }
            "CHOOSE" -> {
                pendingOffers[player.uuid] = Triple(enchantId, level, holder)
                PacketDistributor.sendToPlayer(
                    player,
                    EnchantPreviewPayload(enchantId.toString(), level)
                )
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

        if (!EconomyHandler.removeBalance(player, CHOOSE_COST)) {
            player.sendSystemMessage(Component.translatable("message.estherserver.shop_insufficient"))
            return
        }

        applyEnchantment(player, offer.third, offer.second)
        player.sendSystemMessage(Component.translatable("message.estherserver.enchant_success"))
    }

    fun clearPendingOffer(playerUuid: UUID) {
        pendingOffers.remove(playerUuid)
    }

    private fun applyEnchantment(player: ServerPlayer, enchantHolder: Holder<Enchantment>, level: Int) {
        val item = player.mainHandItem
        val enchantments = item.get(DataComponents.ENCHANTMENTS) ?: ItemEnchantments.EMPTY
        val mutable = ItemEnchantments.Mutable(enchantments)
        mutable.set(enchantHolder, level)
        item.set(DataComponents.ENCHANTMENTS, mutable.toImmutable())
    }

    private fun pickRandomEnchantment(player: ServerPlayer): Pair<Holder<Enchantment>, Int>? {
        val registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
        val allHolders = registry.listElements().toList()
        if (allHolders.isEmpty()) return null

        val holder = allHolders[player.random.nextInt(allHolders.size)]
        val maxLevel = holder.value().maxLevel
        val level = player.random.nextInt(maxLevel) + 1
        return Pair(holder, level)
    }
}
