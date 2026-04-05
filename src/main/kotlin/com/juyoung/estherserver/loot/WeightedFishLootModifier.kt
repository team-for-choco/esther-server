package com.juyoung.estherserver.loot

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
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

        val player = entity.playerOwner as? ServerPlayer ?: return generatedLoot

        val tool = context.getOptionalParameter(LootContextParams.TOOL)
        if (tool == null || tool.item !== EstherServerMod.SPECIAL_FISHING_ROD.get()) return generatedLoot

        val hasVanillaFish = generatedLoot.any {
            BuiltInRegistries.ITEM.getKey(it.item).toString() in VANILLA_FISH_IDS
        }

        if (hasVanillaFish) {
            val equipLevel = EnhancementHandler.getEquipmentLevel(player, Profession.FISHING)
            if (ProfessionBonusHelper.canCatchCustomFish(equipLevel)) {
                // 커스텀 어종 해금 레벨 이상 — 바닐라 물고기 제거 후 해당 등급 풀에서 선택
                generatedLoot.removeIf {
                    BuiltInRegistries.ITEM.getKey(it.item).toString() in VANILLA_FISH_IDS
                }
                val fish = selectRandomFish(context.random, ELIGIBLE_POOLS[equipLevel.coerceAtMost(ELIGIBLE_POOLS.size - 1)])
                if (fish != null) generatedLoot.add(ItemStack(fish))
            }
            // equipLevel == 0: 커스텀 어종 해금 전 — 바닐라 물고기 유지
        }

        return generatedLoot
    }

    private fun selectRandomFish(random: net.minecraft.util.RandomSource, pool: List<WeightedFish>): Item? {
        if (pool.isEmpty()) return null
        val totalWeight = pool.sumOf { it.weight }
        var roll = random.nextInt(totalWeight)
        for (entry in pool) {
            roll -= entry.weight
            if (roll < 0) return entry.item.get()
        }
        return pool.last().item.get()
    }

    override fun codec(): MapCodec<out IGlobalLootModifier> = CODEC

    companion object {
        val VANILLA_FISH_IDS = setOf(
            "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish"
        )

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

        // 레벨별 가능한 풀을 미리 계산 (index = equipLevel, 0은 빈 리스트)
        val ELIGIBLE_POOLS: List<List<WeightedFish>> by lazy {
            (0..5).map { level ->
                if (!ProfessionBonusHelper.canCatchCustomFish(level)) {
                    emptyList()
                } else {
                    val maxGrade = ProfessionBonusHelper.getMaxFishGrade(level)
                    FISH_WEIGHTS.filter { entry ->
                        val grade = ProfessionBonusHelper.getFishGrade(BuiltInRegistries.ITEM.getKey(entry.item.get()))
                        grade != null && grade <= maxGrade
                    }
                }
            }
        }

        val CODEC: MapCodec<WeightedFishLootModifier> = RecordCodecBuilder.mapCodec { inst ->
            codecStart(inst).apply(inst, ::WeightedFishLootModifier)
        }
    }
}
