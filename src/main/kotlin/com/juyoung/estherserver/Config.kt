package com.juyoung.estherserver

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec
import java.util.stream.Collectors

@EventBusSubscriber(modid = EstherServerMod.MODID, bus = EventBusSubscriber.Bus.MOD)
object Config {
    private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

    private val LOG_DIRT_BLOCK: ModConfigSpec.BooleanValue =
        BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true)

    private val MAGIC_NUMBER: ModConfigSpec.IntValue =
        BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Int.MAX_VALUE)

    val MAGIC_NUMBER_INTRODUCTION: ModConfigSpec.ConfigValue<String> =
        BUILDER.comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ")

    private val ITEM_STRINGS: ModConfigSpec.ConfigValue<List<String>> =
        BUILDER.comment("A list of items to log on common setup.").defineListAllowEmpty(
            "items",
            listOf("minecraft:iron_ingot"),
            ::validateItemName
        )

    private val DAYTIME_MULTIPLIER: ModConfigSpec.IntValue =
        BUILDER.comment("Daytime duration multiplier (1 = vanilla, 3 = 3x longer daytime)")
            .defineInRange("daytimeMultiplier", 3, 1, 10)

    private val BALANCE_HUD_X: ModConfigSpec.IntValue =
        BUILDER.comment("Balance HUD X position (negative = offset from right edge, e.g. -5)")
            .defineInRange("balanceHudX", -5, -1000, 1000)

    private val BALANCE_HUD_Y: ModConfigSpec.IntValue =
        BUILDER.comment("Balance HUD Y position (negative = offset from bottom edge, e.g. 5 = top)")
            .defineInRange("balanceHudY", 5, -1000, 1000)

    val SPEC: ModConfigSpec = BUILDER.build()

    var logDirtBlock: Boolean = false
    var magicNumber: Int = 0
    lateinit var magicNumberIntroduction: String
    lateinit var items: Set<Item>
    var daytimeMultiplier: Int = 3
    var balanceHudX: Int = -5
    var balanceHudY: Int = 5

    private fun validateItemName(obj: Any): Boolean {
        return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
    }

    @SubscribeEvent
    fun onLoad(event: ModConfigEvent) {
        logDirtBlock = LOG_DIRT_BLOCK.get()
        magicNumber = MAGIC_NUMBER.get()
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get()

        items = ITEM_STRINGS.get().stream()
            .map { itemName: String? -> ResourceLocation.parse(itemName) }
            .filter { BuiltInRegistries.ITEM.containsKey(it) }
            .map { BuiltInRegistries.ITEM.getValue(it) }
            .collect(Collectors.toSet())

        daytimeMultiplier = DAYTIME_MULTIPLIER.get()
        balanceHudX = BALANCE_HUD_X.get()
        balanceHudY = BALANCE_HUD_Y.get()
    }
}
