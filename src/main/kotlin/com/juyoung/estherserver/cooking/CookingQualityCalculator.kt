package com.juyoung.estherserver.cooking

import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack

object CookingQualityCalculator {
    // Quality score per tier
    private const val COMMON_SCORE = 0
    private const val FINE_SCORE = 1
    private const val RARE_SCORE = 2

    // Probability distributions: [COMMON, FINE, RARE]
    // avgScore=0: 70/25/5 (default)
    // avgScore=1: 50/40/10
    // avgScore=2: 30/55/15
    private val BASE_WEIGHTS = intArrayOf(70, 25, 5)
    private val FINE_WEIGHTS = intArrayOf(50, 40, 10)
    private val RARE_WEIGHTS = intArrayOf(30, 55, 15)

    fun calculateQuality(
        ingredients: List<ItemStack>,
        random: RandomSource,
        fineBonus: Int = 0,
        rareBonus: Int = 0
    ): ItemQuality {
        val avgScore = calculateAverageScore(ingredients)
        val weights = interpolateWeights(avgScore)
        if (fineBonus > 0 || rareBonus > 0) {
            weights[0] = maxOf(10, weights[0] - fineBonus - rareBonus)
            weights[1] = weights[1] + fineBonus
            weights[2] = weights[2] + rareBonus
        }
        return rollQuality(weights, random)
    }

    private fun calculateAverageScore(ingredients: List<ItemStack>): Double {
        if (ingredients.isEmpty()) return 0.0

        val totalScore = ingredients.sumOf { stack ->
            when (stack.get(ModDataComponents.ITEM_QUALITY.get())) {
                ItemQuality.FINE -> FINE_SCORE
                ItemQuality.RARE -> RARE_SCORE
                else -> COMMON_SCORE
            }
        }
        return totalScore.toDouble() / ingredients.size
    }

    private fun interpolateWeights(avgScore: Double): IntArray {
        return when {
            avgScore <= 0.0 -> BASE_WEIGHTS.copyOf()
            avgScore <= 1.0 -> {
                // Interpolate between BASE and FINE
                val t = avgScore
                interpolate(BASE_WEIGHTS, FINE_WEIGHTS, t)
            }
            avgScore <= 2.0 -> {
                // Interpolate between FINE and RARE
                val t = avgScore - 1.0
                interpolate(FINE_WEIGHTS, RARE_WEIGHTS, t)
            }
            else -> RARE_WEIGHTS.copyOf()
        }
    }

    private fun interpolate(from: IntArray, to: IntArray, t: Double): IntArray {
        return IntArray(3) { i ->
            (from[i] + (to[i] - from[i]) * t).toInt()
        }
    }

    private fun rollQuality(weights: IntArray, random: RandomSource): ItemQuality {
        val totalWeight = weights.sum()
        var roll = random.nextInt(totalWeight)
        for ((i, quality) in ItemQuality.entries.withIndex()) {
            roll -= weights[i]
            if (roll < 0) return quality
        }
        return ItemQuality.COMMON
    }
}
