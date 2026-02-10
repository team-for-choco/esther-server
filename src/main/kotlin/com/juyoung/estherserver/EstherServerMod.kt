package com.juyoung.estherserver

import com.mojang.logging.LogUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import com.juyoung.estherserver.block.TestCropBlock
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import com.juyoung.estherserver.loot.ModLootModifiers
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import java.util.function.Consumer
import java.util.function.Supplier

@Mod(EstherServerMod.MODID)
class EstherServerMod(modEventBus: IEventBus, modContainer: ModContainer) {

    companion object {
        const val MODID = "estherserver"
        private val LOGGER = LogUtils.getLogger()

        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        // Example block
        val EXAMPLE_BLOCK: DeferredBlock<Block> =
            BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)

        // Example item
        val EXAMPLE_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "example_item", Item.Properties().food(
                FoodProperties.Builder()
                    .alwaysEdible().nutrition(1).saturationModifier(2f).build()
            )
        )

        // Custom fish - Test Fish
        val TEST_FISH: DeferredItem<Item> = ITEMS.registerSimpleItem("test_fish", Item.Properties())

        val COOKED_TEST_FISH: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "cooked_test_fish", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.8f)
                    .build()
            )
        )

        // Custom crop - Test Crop
        val TEST_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("test_crop",
            { properties -> TestCropBlock(properties) },
            BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.DESTROY))

        val TEST_SEEDS: DeferredItem<Item> = ITEMS.registerItem("test_seeds") { properties ->
            BlockItem(TEST_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val TEST_HARVEST: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "test_harvest", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3f)
                    .build()
            )
        )

        val COOKED_TEST_HARVEST: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "cooked_test_harvest", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.6f)
                    .build()
            )
        )

        // Custom ore - Test Ore
        val TEST_ORE: DeferredBlock<Block> = BLOCKS.registerSimpleBlock(
            "test_ore",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 3.0f)
        )
        val TEST_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("test_ore", TEST_ORE)

        val DEEPSLATE_TEST_ORE: DeferredBlock<Block> = BLOCKS.registerSimpleBlock(
            "deepslate_test_ore",
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .requiresCorrectToolForDrops()
                .strength(4.5f, 3.0f)
                .sound(SoundType.DEEPSLATE)
        )
        val DEEPSLATE_TEST_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_test_ore", DEEPSLATE_TEST_ORE)

        val TEST_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("test_ore_raw", Item.Properties())
        val TEST_ORE_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("test_ore_ingot", Item.Properties())

        // Creative tab
        val ESTHER_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("esther_tab",
            Supplier {
                CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.estherserver"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon { EXAMPLE_ITEM.get().defaultInstance }
                    .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                        output.accept(EXAMPLE_ITEM.get())
                        output.accept(TEST_FISH.get())
                        output.accept(COOKED_TEST_FISH.get())
                        output.accept(TEST_SEEDS.get())
                        output.accept(TEST_HARVEST.get())
                        output.accept(COOKED_TEST_HARVEST.get())
                        output.accept(TEST_ORE.get())
                        output.accept(DEEPSLATE_TEST_ORE.get())
                        output.accept(TEST_ORE_RAW.get())
                        output.accept(TEST_ORE_INGOT.get())
                    }.build()
            })
    }

    init {
        modEventBus.addListener(::commonSetup)

        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus)
        ModDataComponents.DATA_COMPONENTS.register(modEventBus)

        NeoForge.EVENT_BUS.register(this)
        modEventBus.addListener(::addCreative)
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Esther Server mod initialized!")

        if (Config.logDirtBlock) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

        Config.items.forEach(Consumer { item: Item ->
            LOGGER.info("ITEM >> {}", item.toString())
        })
    }

    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey === CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM)
        }
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("Esther Server is starting!")
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        fun onClientSetup(event: FMLClientSetupEvent) {
            LOGGER.info("Esther Server client setup")
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME, value = [Dist.CLIENT])
    object ClientGameEvents {
        @SubscribeEvent
        @JvmStatic
        fun onItemTooltip(event: ItemTooltipEvent) {
            val quality = event.itemStack.get(ModDataComponents.ITEM_QUALITY.get()) ?: return

            if (quality != ItemQuality.COMMON && event.toolTip.isNotEmpty()) {
                event.toolTip[0] = event.toolTip[0].copy().withStyle(quality.color)
            }

            event.toolTip.add(1, Component.translatable(quality.translationKey).withStyle(quality.color))
        }
    }
}
