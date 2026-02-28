package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.neoforged.neoforge.common.loot.IGlobalLootModifier
import net.neoforged.neoforge.common.loot.LootModifier
import java.util.function.Supplier

class WeightedFishLootModifier(
    conditions: Array<LootItemCondition>
) : LootModifier(conditions) {

    data class WeightedFish(val item: Supplier<Item>, val weight: Int)

    override fun doApply(generatedLoot: ObjectArrayList<ItemStack>, context: LootContext): ObjectArrayList<ItemStack> {
        val entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY)
        if (entity !is FishingHook) return generatedLoot

        entity.playerOwner as? ServerPlayer ?: return generatedLoot

        val tool = context.getOptionalParameter(LootContextParams.TOOL)
        if (tool == null || tool.item !== EstherServerMod.SPECIAL_FISHING_ROD.get()) return generatedLoot

        // Replace vanilla fish with weighted custom fish
        val vanillaFishIds = setOf(
            "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish"
        )

        val hasVanillaFish = generatedLoot.any {
            BuiltInRegistries.ITEM.getKey(it.item).toString() in vanillaFishIds
        }

        if (hasVanillaFish) {
            generatedLoot.removeIf {
                BuiltInRegistries.ITEM.getKey(it.item).toString() in vanillaFishIds
            }
            val fish = selectRandomFish(context.random)
            generatedLoot.add(ItemStack(fish))
        }

        return generatedLoot
    }

    private fun selectRandomFish(random: net.minecraft.util.RandomSource): Item {
        val totalWeight = FISH_WEIGHTS.sumOf { it.weight }
        var roll = random.nextInt(totalWeight)
        for (entry in FISH_WEIGHTS) {
            roll -= entry.weight
            if (roll < 0) return entry.item.get()
        }
        return FISH_WEIGHTS.last().item.get()
    }

    override fun codec(): MapCodec<out IGlobalLootModifier> = CODEC

    companion object {
        val FISH_WEIGHTS = listOf(
            // Common (price 5 → weight 17, price 7 → weight 12)
            WeightedFish(Supplier { EstherServerMod.CRUCIAN_CARP.get() }, 17),
            WeightedFish(Supplier { EstherServerMod.SWEETFISH.get() }, 17),
            WeightedFish(Supplier { EstherServerMod.MACKEREL.get() }, 12),
            WeightedFish(Supplier { EstherServerMod.SQUID_CATCH.get() }, 12),
            WeightedFish(Supplier { EstherServerMod.ANCHOVY.get() }, 17),
            WeightedFish(Supplier { EstherServerMod.SHRIMP.get() }, 17),
            WeightedFish(Supplier { EstherServerMod.CLAM.get() }, 17),
            // Advanced (price 15→10, 16→9, 17→7, 18→6)
            WeightedFish(Supplier { EstherServerMod.SALMON_CATCH.get() }, 10),
            WeightedFish(Supplier { EstherServerMod.SEA_BREAM.get() }, 7),
            WeightedFish(Supplier { EstherServerMod.EEL.get() }, 6),
            WeightedFish(Supplier { EstherServerMod.OCTOPUS.get() }, 9),
            WeightedFish(Supplier { EstherServerMod.HAIRTAIL.get() }, 10),
            WeightedFish(Supplier { EstherServerMod.YELLOWTAIL.get() }, 7),
            // Rare (price 45→4, 48→3, 50→3, 53→2, 55→2)
            WeightedFish(Supplier { EstherServerMod.BLUEFIN_TUNA.get() }, 3),
            WeightedFish(Supplier { EstherServerMod.BLOWFISH.get() }, 4),
            WeightedFish(Supplier { EstherServerMod.ABALONE.get() }, 3),
            WeightedFish(Supplier { EstherServerMod.KING_CRAB.get() }, 2),
            WeightedFish(Supplier { EstherServerMod.SEA_URCHIN.get() }, 3),
            WeightedFish(Supplier { EstherServerMod.STURGEON.get() }, 2)
        )

        val CODEC: MapCodec<WeightedFishLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).apply(inst, ::WeightedFishLootModifier)
        }
    }
}
