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
import com.juyoung.estherserver.block.CustomCropBlock
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
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import com.juyoung.estherserver.loot.ModLootModifiers
import com.juyoung.estherserver.quality.ItemQuality
import com.juyoung.estherserver.quality.ModDataComponents
import com.juyoung.estherserver.claim.ClaimCommand
import com.juyoung.estherserver.claim.ClaimProtectionHandler
import com.juyoung.estherserver.claim.LandDeedItem
import com.juyoung.estherserver.collection.ChatTitleHandler
import com.juyoung.estherserver.collection.CollectibleRegistry
import com.juyoung.estherserver.collection.CollectionClientHandler
import com.juyoung.estherserver.collection.CollectionHandler
import com.juyoung.estherserver.collection.CollectionPedestalBlock
import com.juyoung.estherserver.collection.CollectionSyncPayload
import com.juyoung.estherserver.collection.CollectionUpdatePayload
import com.juyoung.estherserver.collection.ModCollection
import com.juyoung.estherserver.collection.TitleCommand
import com.juyoung.estherserver.collection.TitleSelectPayload
import com.juyoung.estherserver.cooking.CookingStationBlock
import com.juyoung.estherserver.cooking.ModCooking
import com.juyoung.estherserver.daylight.DaylightHandler
import com.juyoung.estherserver.economy.BalanceHudOverlay
import com.juyoung.estherserver.economy.BalanceSyncPayload
import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.economy.ItemPriceRegistry
import com.juyoung.estherserver.economy.ModEconomy
import com.juyoung.estherserver.economy.MoneyCommand
import com.juyoung.estherserver.economy.SellCommand
import com.juyoung.estherserver.sitting.ModKeyBindings
import com.juyoung.estherserver.sitting.SeatEntity
import com.juyoung.estherserver.sitting.SitHandler
import com.juyoung.estherserver.sitting.SitPayload
import com.juyoung.estherserver.sleep.SleepHandler
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.GameRules
import net.neoforged.fml.loading.FMLEnvironment
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
        val ENTITY_TYPES: DeferredRegister<EntityType<*>> =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID)

        // Seat entity
        val SEAT_ENTITY: DeferredHolder<EntityType<*>, EntityType<SeatEntity>> =
            ENTITY_TYPES.register("seat", java.util.function.Function { registryName ->
                EntityType.Builder.of(::SeatEntity, MobCategory.MISC)
                    .sized(0.0f, 0.0f)
                    .noSummon()
                    .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, registryName))
            })

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

        // Helper for crop block properties
        private fun cropProperties(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.DESTROY)

        // Custom crop - Test Crop
        val TEST_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("test_crop",
            { properties -> CustomCropBlock(properties, Supplier { TEST_SEEDS.get() }) },
            cropProperties())

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

        // Korean crops - Rice
        val RICE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("rice_crop",
            { properties -> CustomCropBlock(properties, Supplier { RICE_SEEDS.get() }) },
            cropProperties())

        val RICE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("rice_seeds") { properties ->
            BlockItem(RICE_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val RICE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "rice", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.3f)
                    .build()
            )
        )

        val COOKED_RICE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "cooked_rice", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.7f)
                    .build()
            )
        )

        // Korean crops - Red Pepper
        val RED_PEPPER_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("red_pepper_crop",
            { properties -> CustomCropBlock(properties, Supplier { RED_PEPPER_SEEDS.get() }) },
            cropProperties())

        val RED_PEPPER_SEEDS: DeferredItem<Item> = ITEMS.registerItem("red_pepper_seeds") { properties ->
            BlockItem(RED_PEPPER_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val RED_PEPPER: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "red_pepper", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3f)
                    .build()
            )
        )

        // Korean crops - Spinach
        val SPINACH_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("spinach_crop",
            { properties -> CustomCropBlock(properties, Supplier { SPINACH_SEEDS.get() }) },
            cropProperties())

        val SPINACH_SEEDS: DeferredItem<Item> = ITEMS.registerItem("spinach_seeds") { properties ->
            BlockItem(SPINACH_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val SPINACH: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "spinach", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.4f)
                    .build()
            )
        )

        // Cooking station
        val COOKING_STATION: DeferredBlock<Block> = BLOCKS.registerBlock("cooking_station",
            { properties -> CookingStationBlock(properties) },
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0f, 3600000.0f)
                .noLootTable()
        )
        val COOKING_STATION_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("cooking_station", COOKING_STATION)

        // Cooking dishes
        val SPINACH_BIBIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "spinach_bibimbap", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(10)
                    .saturationModifier(0.8f)
                    .build()
            )
        )

        val FISH_STEW: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "fish_stew", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(10)
                    .saturationModifier(0.8f)
                    .build()
            )
        )

        val GIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "gimbap", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(12)
                    .saturationModifier(0.9f)
                    .build()
            )
        )

        val HARVEST_BIBIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "harvest_bibimbap", Item.Properties().food(
                FoodProperties.Builder()
                    .nutrition(14)
                    .saturationModifier(1.0f)
                    .build()
            )
        )

        // Collection pedestal
        val COLLECTION_PEDESTAL: DeferredBlock<Block> = BLOCKS.registerBlock("collection_pedestal",
            { properties -> CollectionPedestalBlock(properties) },
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0f, 3600000.0f)
                .noLootTable()
        )
        val COLLECTION_PEDESTAL_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("collection_pedestal", COLLECTION_PEDESTAL)

        // Land deed
        val LAND_DEED: DeferredItem<Item> = ITEMS.registerItem("land_deed") { properties ->
            LandDeedItem(properties.stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON))
        }

        // Creative tab
        val ESTHER_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("esther_tab",
            Supplier {
                CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.estherserver"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon { TEST_FISH.get().defaultInstance }
                    .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                        output.accept(TEST_FISH.get())
                        output.accept(COOKED_TEST_FISH.get())
                        output.accept(TEST_SEEDS.get())
                        output.accept(TEST_HARVEST.get())
                        output.accept(COOKED_TEST_HARVEST.get())
                        output.accept(TEST_ORE.get())
                        output.accept(DEEPSLATE_TEST_ORE.get())
                        output.accept(TEST_ORE_RAW.get())
                        output.accept(TEST_ORE_INGOT.get())
                        output.accept(RICE_SEEDS.get())
                        output.accept(RICE.get())
                        output.accept(COOKED_RICE.get())
                        output.accept(RED_PEPPER_SEEDS.get())
                        output.accept(RED_PEPPER.get())
                        output.accept(SPINACH_SEEDS.get())
                        output.accept(SPINACH.get())
                        output.accept(COOKING_STATION.get())
                        output.accept(SPINACH_BIBIMBAP.get())
                        output.accept(FISH_STEW.get())
                        output.accept(GIMBAP.get())
                        output.accept(HARVEST_BIBIMBAP.get())
                        output.accept(COLLECTION_PEDESTAL.get())
                        output.accept(LAND_DEED.get())
                    }.build()
            })
    }

    init {
        modEventBus.addListener(::commonSetup)
        modEventBus.addListener(::registerPayloads)

        BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus)
        ModDataComponents.DATA_COMPONENTS.register(modEventBus)
        ENTITY_TYPES.register(modEventBus)
        ModCooking.BLOCK_ENTITY_TYPES.register(modEventBus)
        ModCooking.RECIPE_TYPES.register(modEventBus)
        ModCooking.RECIPE_SERIALIZERS.register(modEventBus)
        ModCollection.ATTACHMENT_TYPES.register(modEventBus)
        ModEconomy.ATTACHMENT_TYPES.register(modEventBus)

        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(SleepHandler)
        NeoForge.EVENT_BUS.register(DaylightHandler)
        NeoForge.EVENT_BUS.register(SitHandler)
        NeoForge.EVENT_BUS.register(CollectionHandler)
        NeoForge.EVENT_BUS.register(ChatTitleHandler)
        NeoForge.EVENT_BUS.register(ClaimProtectionHandler)
        NeoForge.EVENT_BUS.register(EconomyHandler)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(::onItemTooltip)
            NeoForge.EVENT_BUS.register(ModKeyBindings)
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun registerPayloads(event: RegisterPayloadHandlersEvent) {
        event.registrar(MODID)
            .playToServer(SitPayload.TYPE, SitPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    SitHandler.handleSitOnGround(context.player())
                }
            }
            .playToClient(CollectionSyncPayload.TYPE, CollectionSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    CollectionClientHandler.handleSync(payload)
                }
            }
            .playToClient(CollectionUpdatePayload.TYPE, CollectionUpdatePayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    CollectionClientHandler.handleUpdate(payload)
                }
            }
            .playToServer(TitleSelectPayload.TYPE, TitleSelectPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    CollectionHandler.handleTitleSelect(player, payload.milestoneId)
                }
            }
            .playToClient(BalanceSyncPayload.TYPE, BalanceSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    EconomyClientHandler.handleSync(payload)
                }
            }
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Esther Server mod initialized!")
        CollectibleRegistry.init()
        ItemPriceRegistry.init()

        if (Config.logDirtBlock) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))
        }

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

        Config.items.forEach(Consumer { item: Item ->
            LOGGER.info("ITEM >> {}", item.toString())
        })
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        TitleCommand.register(event.dispatcher)
        ClaimCommand.register(event.dispatcher)
        MoneyCommand.register(event.dispatcher)
        SellCommand.register(event.dispatcher)
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        LOGGER.info("Esther Server is starting!")

        val server = event.server
        server.gameRules.getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(0, server)
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        fun onClientSetup(event: FMLClientSetupEvent) {
            LOGGER.info("Esther Server client setup")
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }

        @SubscribeEvent
        fun onRegisterKeyMappings(event: net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent) {
            event.register(ModKeyBindings.SIT_KEY)
            event.register(ModKeyBindings.COLLECTION_KEY)
        }

        @SubscribeEvent
        fun onRegisterGuiLayers(event: net.neoforged.neoforge.client.event.RegisterGuiLayersEvent) {
            BalanceHudOverlay.registerLayer(event)
        }

        @SubscribeEvent
        fun onRegisterRenderers(event: net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers) {
            event.registerEntityRenderer(SEAT_ENTITY.get()) { context ->
                net.minecraft.client.renderer.entity.NoopRenderer(context)
            }
        }
    }

    private fun onItemTooltip(event: ItemTooltipEvent) {
        val quality = event.itemStack.get(ModDataComponents.ITEM_QUALITY.get()) ?: return

        if (event.toolTip.isNotEmpty()) {
            if (quality != ItemQuality.COMMON) {
                event.toolTip[0] = event.toolTip[0].copy().withStyle(quality.color)
            }
            event.toolTip.add(1, Component.translatable(quality.translationKey).withStyle(quality.color))
        }
    }
}
