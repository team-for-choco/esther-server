package com.juyoung.estherserver.profession

import net.minecraft.util.RandomSource

object ProfessionBonusHelper {

    // --- Profession Level Bonuses ---

    fun getXpMultiplier(profLevel: Int): Double = when {
        profLevel >= 30 -> 1.5
        profLevel >= 10 -> 1.2
        else -> 1.0
    }

    fun getFineQualityBonus(profLevel: Int): Int = when {
        profLevel >= 20 -> 5
        else -> 0
    }

    fun getRareQualityBonus(profLevel: Int): Int = when {
        profLevel >= 40 -> 3
        else -> 0
    }

    // --- Equipment Enhancement Bonuses ---

    fun getFishingLureBonus(equipLevel: Int): Int = when {
        equipLevel >= 5 -> 40
        equipLevel >= 3 -> 20
        else -> 0
    }

    fun getExtraHarvestChance(equipLevel: Int): Float = when {
        equipLevel >= 5 -> 0.50f
        equipLevel >= 3 -> 0.25f
        else -> 0f
    }

    fun getCookingRareBonus(equipLevel: Int): Int = when {
        equipLevel >= 5 -> 10
        equipLevel >= 3 -> 5
        else -> 0
    }

    // --- Lv50 Unique Bonuses ---

    fun shouldDoubleMineDrop(profLevel: Int, random: RandomSource): Boolean =
        profLevel >= 50 && random.nextFloat() < 0.30f

    fun shouldDoubleFish(profLevel: Int, random: RandomSource): Boolean =
        profLevel >= 50 && random.nextFloat() < 0.25f

    fun shouldPreserveSeed(profLevel: Int, random: RandomSource): Boolean =
        profLevel >= 50 && random.nextFloat() < 0.30f

    fun shouldSaveIngredient(profLevel: Int, random: RandomSource): Boolean =
        profLevel >= 50 && random.nextFloat() < 0.25f
}
