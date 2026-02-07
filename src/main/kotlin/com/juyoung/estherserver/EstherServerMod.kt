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
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
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
        val TEST_CROP: DeferredBlock<Block> = BLOCKS.register("test_crop",
            Supplier { TestCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)) })

        val TEST_SEEDS: DeferredItem<Item> = ITEMS.register("test_seeds",
            Supplier { BlockItem(TEST_CROP.get(), Item.Properties().useItemDescriptionPrefix()) })

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
                    }.build()
            })
    }

    init {
        modEventBus.addListener(::commonSetup)

        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus)

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
}
