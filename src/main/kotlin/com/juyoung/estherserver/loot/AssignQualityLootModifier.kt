package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.profession.ProfessionHandler
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier

class AssignQualityLootModifier(
    conditions: Array<LootItemCondition>
) : LootModifier(conditions) {

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        val player = getPlayerFromContext(context)

        for (stack in generatedLoot) {
            if (stack.`is`(HAS_QUALITY_TAG)) {
                val quality = ItemQuality.randomQuality(context.random)
                stack.set(ModDataComponents.ITEM_QUALITY.get(), quality)

                // Grant profession XP
                if (player != null) {
                    val profession = ProfessionHandler.getProfessionForItem(stack)
                    if (profession != null) {
                        val xp = ProfessionHandler.getXpForQuality(quality)
                        ProfessionHandler.addExperience(player, profession, xp)
                    }
                }
            }
        }
        return generatedLoot
    }

    private fun getPlayerFromContext(context: LootContext): ServerPlayer? {
        val entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY)
        if (entity is ServerPlayer) return entity
        if (entity is FishingHook) return entity.playerOwner as? ServerPlayer
        return null
    }

    override fun codec(): MapCodec<out IGlobalLootModifier> = CODEC

    companion object {
        val HAS_QUALITY_TAG: TagKey<Item> = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "has_quality")
        )

        val CODEC: MapCodec<AssignQualityLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).apply(inst, ::AssignQualityLootModifier)
        }
    }
}
