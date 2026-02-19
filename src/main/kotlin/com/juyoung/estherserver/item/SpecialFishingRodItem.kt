package com.juyoung.estherserver.item

import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.FishingRodItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent

class SpecialFishingRodItem(properties: Properties) : FishingRodItem(properties) {

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val itemstack = player.getItemInHand(hand)

        if (player.fishing != null) {
            if (!level.isClientSide) {
                val damage = player.fishing!!.retrieve(itemstack)
                val original = itemstack.copy()
                itemstack.hurtAndBreak(damage, player, LivingEntity.getSlotForHand(hand))
                if (itemstack.isEmpty) {
                    net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, original, hand)
                }
            }
            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL,
                1.0f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
            )
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH)
        } else {
            level.playSound(
                null, player.x, player.y, player.z,
                SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f)
            )
            if (level is ServerLevel) {
                val lureEnchant = (EnchantmentHelper.getFishingTimeReduction(level, itemstack, player) * 20.0f).toInt()
                val luckEnchant = EnchantmentHelper.getFishingLuckBonus(level, itemstack, player)

                // Add equipment enhancement lure bonus
                val equipLevel = itemstack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)
                val lureBonus = ProfessionBonusHelper.getFishingLureBonus(equipLevel)
                val totalLure = lureEnchant + lureBonus

                Projectile.spawnProjectile(
                    FishingHook(player, level, luckEnchant, totalLure),
                    level, itemstack
                )
            }
            player.awardStat(Stats.ITEM_USED[this])
            player.gameEvent(GameEvent.ITEM_INTERACT_START)
        }

        return InteractionResult.SUCCESS
    }
}
