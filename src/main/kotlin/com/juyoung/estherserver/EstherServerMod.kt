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
import net.neoforged.neoforge.network.PacketDistributor
import com.juyoung.estherserver.block.SpecialFarmlandBlock
import com.juyoung.estherserver.item.SpecialFishingRodItem
import com.juyoung.estherserver.item.SprayerItem
import com.juyoung.estherserver.item.WateringCanItem
import net.minecraft.core.component.DataComponents
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.component.Tool
import net.neoforged.neoforge.event.level.block.CropGrowEvent
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
import com.juyoung.estherserver.claim.ChunkClaimManager
import com.juyoung.estherserver.loot.ModLootModifiers
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
import com.juyoung.estherserver.collection.RewardClaimPayload
import com.juyoung.estherserver.collection.TitleSelectPayload
import com.juyoung.estherserver.cooking.CookingStationBlock
import com.juyoung.estherserver.cooking.ModCooking
import com.juyoung.estherserver.inventory.ModInventory
import com.juyoung.estherserver.inventory.ProfessionInventoryClientHandler
import com.juyoung.estherserver.inventory.ProfessionInventoryContainerScreen
import com.juyoung.estherserver.inventory.ProfessionInventoryHandler
import com.juyoung.estherserver.inventory.ProfessionInventoryMenu
import com.juyoung.estherserver.inventory.ProfessionInventoryPayload
import com.juyoung.estherserver.daylight.DaylightHandler
import com.juyoung.estherserver.economy.BalanceHudOverlay
import com.juyoung.estherserver.economy.BalanceSyncPayload
import com.juyoung.estherserver.economy.EconomyClientHandler
import com.juyoung.estherserver.economy.EconomyHandler
import com.juyoung.estherserver.economy.ItemPriceRegistry
import com.juyoung.estherserver.economy.ModEconomy
import com.juyoung.estherserver.economy.MoneyCommand
import com.juyoung.estherserver.enhancement.EnhanceItemPayload
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.ModProfession
import com.juyoung.estherserver.profession.ProfessionClientHandler
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionCommand
import com.juyoung.estherserver.profession.ProfessionHandler
import com.juyoung.estherserver.profession.ProfessionSyncPayload
import com.juyoung.estherserver.merchant.SellItemPayload
import com.juyoung.estherserver.merchant.BuyItemPayload
import com.juyoung.estherserver.merchant.MerchantEntity
import com.juyoung.estherserver.merchant.MerchantEntityRenderer
import com.juyoung.estherserver.merchant.OpenShopPayload
import com.juyoung.estherserver.merchant.ShopBuyRegistry
import com.juyoung.estherserver.merchant.ShopClientHandler
import com.juyoung.estherserver.merchant.ShopCommand
import com.juyoung.estherserver.furniture.CatSofaBlock
import com.juyoung.estherserver.furniture.CatSofaDummyBlock
import com.juyoung.estherserver.furniture.DogSofaBlock
import com.juyoung.estherserver.furniture.DogSofaDummyBlock
import com.juyoung.estherserver.furniture.RabbitSofaBlock
import com.juyoung.estherserver.furniture.RabbitSofaDummyBlock
import com.juyoung.estherserver.furniture.FoxSofaBlock
import com.juyoung.estherserver.furniture.FoxSofaDummyBlock
import com.juyoung.estherserver.furniture.ModFurniture
import com.juyoung.estherserver.quest.ModQuest
import com.juyoung.estherserver.quest.QuestBonusClaimPayload
import com.juyoung.estherserver.quest.QuestClaimPayload
import com.juyoung.estherserver.quest.QuestClientHandler
import com.juyoung.estherserver.quest.QuestCommand
import com.juyoung.estherserver.quest.QuestHandler
import com.juyoung.estherserver.quest.QuestOpenScreenPayload
import com.juyoung.estherserver.quest.QuestScreen
import com.juyoung.estherserver.quest.QuestSyncPayload
import com.juyoung.estherserver.cosmetic.CosmeticArmorItems
import com.juyoung.estherserver.cosmetic.CosmeticBroadcastPayload
import com.juyoung.estherserver.cosmetic.CosmeticClientHandler
import com.juyoung.estherserver.cosmetic.CosmeticHandler
import com.juyoung.estherserver.cosmetic.CosmeticRegistry
import com.juyoung.estherserver.cosmetic.CosmeticScreen
import com.juyoung.estherserver.cosmetic.CosmeticSyncPayload
import com.juyoung.estherserver.cosmetic.CosmeticTokenItem
import com.juyoung.estherserver.cosmetic.EquipCosmeticPayload
import com.juyoung.estherserver.cosmetic.ModCosmetics
import com.juyoung.estherserver.cosmetic.RequestCosmeticsPayload
import com.juyoung.estherserver.gacha.GachaClientHandler
import com.juyoung.estherserver.gacha.GachaRegistry
import com.juyoung.estherserver.gacha.GachaRoulettePayload
import com.juyoung.estherserver.gacha.GachaRouletteScreen
import com.juyoung.estherserver.gacha.GachaTicketItem
import com.juyoung.estherserver.pet.ModPets
import com.juyoung.estherserver.pet.PetClientHandler
import com.juyoung.estherserver.pet.PetEntity
import com.juyoung.estherserver.pet.PetEntityModel
import com.juyoung.estherserver.pet.PetEntityRenderer
import com.juyoung.estherserver.pet.PetHandler
import com.juyoung.estherserver.pet.PetStorageScreen
import com.juyoung.estherserver.pet.PetTokenItem
import com.juyoung.estherserver.pet.PetType
import com.juyoung.estherserver.pet.PetStorageSyncPayload
import com.juyoung.estherserver.pet.RequestPetStoragePayload
import com.juyoung.estherserver.pet.SummonPetPayload
import com.juyoung.estherserver.sitting.ModKeyBindings
import com.juyoung.estherserver.sitting.SeatEntity
import com.juyoung.estherserver.sitting.SitHandler
import com.juyoung.estherserver.sitting.SitPayload
import com.juyoung.estherserver.sleep.SleepHandler
import com.juyoung.estherserver.wild.ModWild
import com.juyoung.estherserver.wild.PortalDummyBlock
import com.juyoung.estherserver.wild.ReturnPortalBlock
import com.juyoung.estherserver.wild.WildCommand
import com.juyoung.estherserver.wild.WildPortalBlock
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.GameRules
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
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

        // Merchant NPC entity
        val MERCHANT_ENTITY: DeferredHolder<EntityType<*>, EntityType<MerchantEntity>> =
            ENTITY_TYPES.register("merchant", java.util.function.Function { registryName ->
                EntityType.Builder.of(::MerchantEntity, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, registryName))
            })

        // Pet mount entity
        val PET_ENTITY: DeferredHolder<EntityType<*>, EntityType<PetEntity>> =
            ENTITY_TYPES.register("pet", java.util.function.Function { registryName ->
                EntityType.Builder.of(::PetEntity, MobCategory.CREATURE)
                    .sized(0.6f, 0.5f)
                    .noSummon()
                    .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, registryName))
            })

        // Helper for crop block properties
        private fun cropProperties(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(PushReaction.DESTROY)

        // Korean crops - Rice
        val RICE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("rice_crop",
            { properties -> CustomCropBlock(properties, Supplier { RICE_SEEDS.get() }) },
            cropProperties())

        val RICE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("rice_seeds") { properties ->
            BlockItem(RICE_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val RICE: DeferredItem<Item> = ITEMS.registerSimpleItem("rice", Item.Properties())

        val COOKED_RICE: DeferredItem<Item> = ITEMS.registerSimpleItem("cooked_rice", Item.Properties())

        // Korean crops - Red Pepper
        val RED_PEPPER_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("red_pepper_crop",
            { properties -> CustomCropBlock(properties, Supplier { RED_PEPPER_SEEDS.get() }) },
            cropProperties())

        val RED_PEPPER_SEEDS: DeferredItem<Item> = ITEMS.registerItem("red_pepper_seeds") { properties ->
            BlockItem(RED_PEPPER_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val RED_PEPPER: DeferredItem<Item> = ITEMS.registerSimpleItem("red_pepper", Item.Properties())

        // Korean crops - Spinach
        val SPINACH_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("spinach_crop",
            { properties -> CustomCropBlock(properties, Supplier { SPINACH_SEEDS.get() }) },
            cropProperties())

        val SPINACH_SEEDS: DeferredItem<Item> = ITEMS.registerItem("spinach_seeds") { properties ->
            BlockItem(SPINACH_CROP.get(), properties.useItemDescriptionPrefix())
        }

        val SPINACH: DeferredItem<Item> = ITEMS.registerSimpleItem("spinach", Item.Properties())

        // New crops - Common
        val GREEN_ONION_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("green_onion_crop",
            { properties -> CustomCropBlock(properties, Supplier { GREEN_ONION_SEEDS.get() }) }, cropProperties())
        val GREEN_ONION_SEEDS: DeferredItem<Item> = ITEMS.registerItem("green_onion_seeds") { properties ->
            BlockItem(GREEN_ONION_CROP.get(), properties.useItemDescriptionPrefix()) }
        val GREEN_ONION: DeferredItem<Item> = ITEMS.registerSimpleItem("green_onion")

        val GARLIC_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("garlic_crop",
            { properties -> CustomCropBlock(properties, Supplier { GARLIC_SEEDS.get() }) }, cropProperties())
        val GARLIC_SEEDS: DeferredItem<Item> = ITEMS.registerItem("garlic_seeds") { properties ->
            BlockItem(GARLIC_CROP.get(), properties.useItemDescriptionPrefix()) }
        val GARLIC: DeferredItem<Item> = ITEMS.registerSimpleItem("garlic")

        val CABBAGE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("cabbage_crop",
            { properties -> CustomCropBlock(properties, Supplier { CABBAGE_SEEDS.get() }) }, cropProperties())
        val CABBAGE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("cabbage_seeds") { properties ->
            BlockItem(CABBAGE_CROP.get(), properties.useItemDescriptionPrefix()) }
        val CABBAGE: DeferredItem<Item> = ITEMS.registerSimpleItem("cabbage")

        val SOYBEAN_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("soybean_crop",
            { properties -> CustomCropBlock(properties, Supplier { SOYBEAN_SEEDS.get() }) }, cropProperties())
        val SOYBEAN_SEEDS: DeferredItem<Item> = ITEMS.registerItem("soybean_seeds") { properties ->
            BlockItem(SOYBEAN_CROP.get(), properties.useItemDescriptionPrefix()) }
        val SOYBEAN: DeferredItem<Item> = ITEMS.registerSimpleItem("soybean")

        val SESAME_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("sesame_crop",
            { properties -> CustomCropBlock(properties, Supplier { SESAME_SEEDS.get() }) }, cropProperties())
        val SESAME_SEEDS: DeferredItem<Item> = ITEMS.registerItem("sesame_seeds") { properties ->
            BlockItem(SESAME_CROP.get(), properties.useItemDescriptionPrefix()) }
        val SESAME: DeferredItem<Item> = ITEMS.registerSimpleItem("sesame")

        // New crops - Advanced
        val GINGER_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("ginger_crop",
            { properties -> CustomCropBlock(properties, Supplier { GINGER_SEEDS.get() }) }, cropProperties())
        val GINGER_SEEDS: DeferredItem<Item> = ITEMS.registerItem("ginger_seeds") { properties ->
            BlockItem(GINGER_CROP.get(), properties.useItemDescriptionPrefix()) }
        val GINGER: DeferredItem<Item> = ITEMS.registerSimpleItem("ginger")

        val PERILLA_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("perilla_crop",
            { properties -> CustomCropBlock(properties, Supplier { PERILLA_SEEDS.get() }) }, cropProperties())
        val PERILLA_SEEDS: DeferredItem<Item> = ITEMS.registerItem("perilla_seeds") { properties ->
            BlockItem(PERILLA_CROP.get(), properties.useItemDescriptionPrefix()) }
        val PERILLA: DeferredItem<Item> = ITEMS.registerSimpleItem("perilla")

        val LOTUS_ROOT_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("lotus_root_crop",
            { properties -> CustomCropBlock(properties, Supplier { LOTUS_ROOT_SEEDS.get() }) }, cropProperties())
        val LOTUS_ROOT_SEEDS: DeferredItem<Item> = ITEMS.registerItem("lotus_root_seeds") { properties ->
            BlockItem(LOTUS_ROOT_CROP.get(), properties.useItemDescriptionPrefix()) }
        val LOTUS_ROOT: DeferredItem<Item> = ITEMS.registerSimpleItem("lotus_root")

        val SHIITAKE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("shiitake_crop",
            { properties -> CustomCropBlock(properties, Supplier { SHIITAKE_SEEDS.get() }) }, cropProperties())
        val SHIITAKE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("shiitake_seeds") { properties ->
            BlockItem(SHIITAKE_CROP.get(), properties.useItemDescriptionPrefix()) }
        val SHIITAKE: DeferredItem<Item> = ITEMS.registerSimpleItem("shiitake")

        val BAMBOO_SHOOT_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("bamboo_shoot_crop",
            { properties -> CustomCropBlock(properties, Supplier { BAMBOO_SHOOT_SEEDS.get() }) }, cropProperties())
        val BAMBOO_SHOOT_SEEDS: DeferredItem<Item> = ITEMS.registerItem("bamboo_shoot_seeds") { properties ->
            BlockItem(BAMBOO_SHOOT_CROP.get(), properties.useItemDescriptionPrefix()) }
        val BAMBOO_SHOOT: DeferredItem<Item> = ITEMS.registerSimpleItem("bamboo_shoot")

        val WASABI_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("wasabi_crop",
            { properties -> CustomCropBlock(properties, Supplier { WASABI_SEEDS.get() }) }, cropProperties())
        val WASABI_SEEDS: DeferredItem<Item> = ITEMS.registerItem("wasabi_seeds") { properties ->
            BlockItem(WASABI_CROP.get(), properties.useItemDescriptionPrefix()) }
        val WASABI: DeferredItem<Item> = ITEMS.registerSimpleItem("wasabi")

        // New crops - Rare
        val GINSENG_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("ginseng_crop",
            { properties -> CustomCropBlock(properties, Supplier { GINSENG_SEEDS.get() }) }, cropProperties())
        val GINSENG_SEEDS: DeferredItem<Item> = ITEMS.registerItem("ginseng_seeds") { properties ->
            BlockItem(GINSENG_CROP.get(), properties.useItemDescriptionPrefix()) }
        val GINSENG: DeferredItem<Item> = ITEMS.registerSimpleItem("ginseng")

        val TRUFFLE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("truffle_crop",
            { properties -> CustomCropBlock(properties, Supplier { TRUFFLE_SEEDS.get() }) }, cropProperties())
        val TRUFFLE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("truffle_seeds") { properties ->
            BlockItem(TRUFFLE_CROP.get(), properties.useItemDescriptionPrefix()) }
        val TRUFFLE: DeferredItem<Item> = ITEMS.registerSimpleItem("truffle")

        val SAFFRON_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("saffron_crop",
            { properties -> CustomCropBlock(properties, Supplier { SAFFRON_SEEDS.get() }) }, cropProperties())
        val SAFFRON_SEEDS: DeferredItem<Item> = ITEMS.registerItem("saffron_seeds") { properties ->
            BlockItem(SAFFRON_CROP.get(), properties.useItemDescriptionPrefix()) }
        val SAFFRON: DeferredItem<Item> = ITEMS.registerSimpleItem("saffron")

        val MATSUTAKE_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("matsutake_crop",
            { properties -> CustomCropBlock(properties, Supplier { MATSUTAKE_SEEDS.get() }) }, cropProperties())
        val MATSUTAKE_SEEDS: DeferredItem<Item> = ITEMS.registerItem("matsutake_seeds") { properties ->
            BlockItem(MATSUTAKE_CROP.get(), properties.useItemDescriptionPrefix()) }
        val MATSUTAKE: DeferredItem<Item> = ITEMS.registerSimpleItem("matsutake")

        val YUZU_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("yuzu_crop",
            { properties -> CustomCropBlock(properties, Supplier { YUZU_SEEDS.get() }) }, cropProperties())
        val YUZU_SEEDS: DeferredItem<Item> = ITEMS.registerItem("yuzu_seeds") { properties ->
            BlockItem(YUZU_CROP.get(), properties.useItemDescriptionPrefix()) }
        val YUZU: DeferredItem<Item> = ITEMS.registerSimpleItem("yuzu")

        val GREEN_TEA_CROP: DeferredBlock<Block> = BLOCKS.registerBlock("green_tea_crop",
            { properties -> CustomCropBlock(properties, Supplier { GREEN_TEA_SEEDS.get() }) }, cropProperties())
        val GREEN_TEA_SEEDS: DeferredItem<Item> = ITEMS.registerItem("green_tea_seeds") { properties ->
            BlockItem(GREEN_TEA_CROP.get(), properties.useItemDescriptionPrefix()) }
        val GREEN_TEA: DeferredItem<Item> = ITEMS.registerSimpleItem("green_tea")

        // Fish - Common
        val CRUCIAN_CARP: DeferredItem<Item> = ITEMS.registerSimpleItem("crucian_carp")
        val SWEETFISH: DeferredItem<Item> = ITEMS.registerSimpleItem("sweetfish")
        val MACKEREL: DeferredItem<Item> = ITEMS.registerSimpleItem("mackerel")
        val SQUID_CATCH: DeferredItem<Item> = ITEMS.registerSimpleItem("squid_catch")
        val ANCHOVY: DeferredItem<Item> = ITEMS.registerSimpleItem("anchovy")
        val SHRIMP: DeferredItem<Item> = ITEMS.registerSimpleItem("shrimp")
        val CLAM: DeferredItem<Item> = ITEMS.registerSimpleItem("clam")

        // Fish - Advanced
        val SALMON_CATCH: DeferredItem<Item> = ITEMS.registerSimpleItem("salmon_catch")
        val SEA_BREAM: DeferredItem<Item> = ITEMS.registerSimpleItem("sea_bream")
        val EEL: DeferredItem<Item> = ITEMS.registerSimpleItem("eel")
        val OCTOPUS: DeferredItem<Item> = ITEMS.registerSimpleItem("octopus")
        val HAIRTAIL: DeferredItem<Item> = ITEMS.registerSimpleItem("hairtail")
        val YELLOWTAIL: DeferredItem<Item> = ITEMS.registerSimpleItem("yellowtail")

        // Fish - Rare
        val BLUEFIN_TUNA: DeferredItem<Item> = ITEMS.registerSimpleItem("bluefin_tuna")
        val BLOWFISH: DeferredItem<Item> = ITEMS.registerSimpleItem("blowfish")
        val ABALONE: DeferredItem<Item> = ITEMS.registerSimpleItem("abalone")
        val KING_CRAB: DeferredItem<Item> = ITEMS.registerSimpleItem("king_crab")
        val SEA_URCHIN: DeferredItem<Item> = ITEMS.registerSimpleItem("sea_urchin")
        val STURGEON: DeferredItem<Item> = ITEMS.registerSimpleItem("sturgeon")

        // Minerals - Common ores
        val TIN_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("tin_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE))
        val DEEPSLATE_TIN_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_tin_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE))
        val TIN_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("tin_ore", TIN_ORE)
        val DEEPSLATE_TIN_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_tin_ore", DEEPSLATE_TIN_ORE)
        val TIN_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("tin_ore_raw")
        val TIN_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("tin_ingot")

        val ZINC_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("zinc_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE))
        val DEEPSLATE_ZINC_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_zinc_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE))
        val ZINC_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("zinc_ore", ZINC_ORE)
        val DEEPSLATE_ZINC_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_zinc_ore", DEEPSLATE_ZINC_ORE)
        val ZINC_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("zinc_ore_raw")
        val ZINC_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("zinc_ingot")

        val JADE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("jade_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE))
        val DEEPSLATE_JADE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_jade_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE))
        val JADE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("jade_ore", JADE_ORE)
        val DEEPSLATE_JADE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_jade_ore", DEEPSLATE_JADE_ORE)
        val JADE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("jade_raw")

        // Minerals - Advanced ores
        val SILVER_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("silver_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE))
        val DEEPSLATE_SILVER_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_silver_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE))
        val SILVER_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("silver_ore", SILVER_ORE)
        val DEEPSLATE_SILVER_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_silver_ore", DEEPSLATE_SILVER_ORE)
        val SILVER_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("silver_ore_raw")
        val SILVER_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("silver_ingot")

        val RUBY_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("ruby_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE))
        val DEEPSLATE_RUBY_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_ruby_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE))
        val RUBY_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("ruby_ore", RUBY_ORE)
        val DEEPSLATE_RUBY_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_ruby_ore", DEEPSLATE_RUBY_ORE)
        val RUBY_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("ruby_raw")

        val SAPPHIRE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("sapphire_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE))
        val DEEPSLATE_SAPPHIRE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_sapphire_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE))
        val SAPPHIRE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("sapphire_ore", SAPPHIRE_ORE)
        val DEEPSLATE_SAPPHIRE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_sapphire_ore", DEEPSLATE_SAPPHIRE_ORE)
        val SAPPHIRE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("sapphire_raw")

        val TITANIUM_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("titanium_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE))
        val DEEPSLATE_TITANIUM_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_titanium_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE))
        val TITANIUM_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("titanium_ore", TITANIUM_ORE)
        val DEEPSLATE_TITANIUM_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_titanium_ore", DEEPSLATE_TITANIUM_ORE)
        val TITANIUM_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("titanium_ore_raw")
        val TITANIUM_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("titanium_ingot")

        // Minerals - Rare ores
        val PLATINUM_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("platinum_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE))
        val DEEPSLATE_PLATINUM_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_platinum_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE))
        val PLATINUM_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("platinum_ore", PLATINUM_ORE)
        val DEEPSLATE_PLATINUM_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_platinum_ore", DEEPSLATE_PLATINUM_ORE)
        val PLATINUM_ORE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("platinum_ore_raw")
        val PLATINUM_INGOT: DeferredItem<Item> = ITEMS.registerSimpleItem("platinum_ingot")

        val OPAL_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("opal_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE))
        val DEEPSLATE_OPAL_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_opal_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE))
        val OPAL_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("opal_ore", OPAL_ORE)
        val DEEPSLATE_OPAL_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_opal_ore", DEEPSLATE_OPAL_ORE)
        val OPAL_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("opal_raw")

        val TANZANITE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("tanzanite_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE))
        val DEEPSLATE_TANZANITE_ORE: DeferredBlock<Block> = BLOCKS.registerBlock("deepslate_tanzanite_ore", ::Block, BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE))
        val TANZANITE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("tanzanite_ore", TANZANITE_ORE)
        val DEEPSLATE_TANZANITE_ORE_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("deepslate_tanzanite_ore", DEEPSLATE_TANZANITE_ORE)
        val TANZANITE_RAW: DeferredItem<Item> = ITEMS.registerSimpleItem("tanzanite_raw")

        // Obsidian shard (smelted from obsidian)
        val OBSIDIAN_SHARD: DeferredItem<Item> = ITEMS.registerSimpleItem("obsidian_shard")

        // Cooking station
        val COOKING_STATION: DeferredBlock<Block> = BLOCKS.registerBlock("cooking_station",
            { properties -> CookingStationBlock(properties) },
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0f, 3600000.0f)
                .noLootTable()
                .noOcclusion()
        )
        val COOKING_STATION_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("cooking_station", COOKING_STATION)

        // Standard cooking FoodProperties: all dishes use same nutrition
        private fun cookingFood(): FoodProperties = FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6f).build()

        // Cooking ingredients (no food properties, acquire method TBD)
        val SEAWEED: DeferredItem<Item> = ITEMS.registerSimpleItem("seaweed")
        val NOODLES: DeferredItem<Item> = ITEMS.registerSimpleItem("noodles")

        // Cooking dishes - Common
        val SPINACH_BIBIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "spinach_bibimbap", Item.Properties().food(cookingFood()))
        val FISH_STEW: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "fish_stew", Item.Properties().food(cookingFood()))
        val GIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "gimbap", Item.Properties().food(cookingFood()))
        val KIMCHI: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "kimchi", Item.Properties().food(cookingFood()))
        val KIMCHI_STEW: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "kimchi_stew", Item.Properties().food(cookingFood()))
        val MISO_SOUP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "miso_soup", Item.Properties().food(cookingFood()))
        val GRILLED_MACKEREL: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "grilled_mackerel", Item.Properties().food(cookingFood()))
        val EGG_RICE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "egg_rice", Item.Properties().food(cookingFood()))

        // Cooking dishes - Advanced
        val SASHIMI_PLATTER: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "sashimi_platter", Item.Properties().food(cookingFood()))
        val EEL_RICE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "eel_rice", Item.Properties().food(cookingFood()))
        val DUMPLING: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "dumpling", Item.Properties().food(cookingFood()))
        val JAPCHAE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "japchae", Item.Properties().food(cookingFood()))
        val RAMEN: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "ramen", Item.Properties().food(cookingFood()))
        val MAPO_TOFU: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "mapo_tofu", Item.Properties().food(cookingFood()))
        val SEAFOOD_PANCAKE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "seafood_pancake", Item.Properties().food(cookingFood()))
        val LOTUS_SALAD: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "lotus_salad", Item.Properties().food(cookingFood()))

        // Cooking dishes - Rare
        val GINSENG_CHICKEN: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "ginseng_chicken", Item.Properties().food(cookingFood()))
        val TRUFFLE_RISOTTO: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "truffle_risotto", Item.Properties().food(cookingFood()))
        val BLOWFISH_SASHIMI: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "blowfish_sashimi", Item.Properties().food(cookingFood()))
        val ROYAL_BIBIMBAP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "royal_bibimbap", Item.Properties().food(cookingFood()))
        val MATSUTAKE_SOUP: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "matsutake_soup", Item.Properties().food(cookingFood()))
        val SAFFRON_RICE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "saffron_rice", Item.Properties().food(cookingFood()))
        val ABALONE_PORRIDGE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "abalone_porridge", Item.Properties().food(cookingFood()))
        val KING_CRAB_STEW: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "king_crab_stew", Item.Properties().food(cookingFood()))

        // Collection pedestal
        val COLLECTION_PEDESTAL: DeferredBlock<Block> = BLOCKS.registerBlock("collection_pedestal",
            { properties -> CollectionPedestalBlock(properties) },
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0f, 3600000.0f)
                .noLootTable()
                .noOcclusion()
        )
        val COLLECTION_PEDESTAL_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("collection_pedestal", COLLECTION_PEDESTAL)

        // Land deed
        val LAND_DEED: DeferredItem<Item> = ITEMS.registerItem("land_deed") { properties ->
            LandDeedItem(properties.stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON))
        }

        // Specialty equipment (stacksTo 1, no durability)
        val SPECIAL_FISHING_ROD: DeferredItem<Item> = ITEMS.registerItem("special_fishing_rod") { properties ->
            SpecialFishingRodItem(properties.stacksTo(1))
        }
        val SPECIAL_HOE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "special_hoe", Item.Properties().stacksTo(1)
        )
        val SPECIAL_PICKAXE: DeferredItem<Item> = ITEMS.registerItem("special_pickaxe") { properties ->
            val blockLookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)
            Item(properties.stacksTo(1).component(
                DataComponents.TOOL,
                Tool(
                    listOf(Tool.Rule.minesAndDrops(
                        blockLookup.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), 4.0f
                    )),
                    1.0f,
                    0
                )
            ))
        }
        val SPECIAL_COOKING_TOOL: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "special_cooking_tool", Item.Properties().stacksTo(1)
        )

        // Special farmland block
        val SPECIAL_FARMLAND: DeferredBlock<Block> = BLOCKS.registerBlock("special_farmland",
            ::SpecialFarmlandBlock,
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.6f)
                .sound(SoundType.GRAVEL)
                .isViewBlocking { _, _, _ -> true }
                .isSuffocating { _, _, _ -> true }
        )
        val SPECIAL_FARMLAND_ITEM: DeferredItem<BlockItem> =
            ITEMS.registerSimpleBlockItem("special_farmland", SPECIAL_FARMLAND)

        // Sprayer item
        val SPRAYER: DeferredItem<Item> = ITEMS.registerItem("sprayer") { properties ->
            SprayerItem(properties.stacksTo(1))
        }

        // Watering can (upgraded sprayer, requires hoe Lv3+)
        val WATERING_CAN: DeferredItem<Item> = ITEMS.registerItem("watering_can") { properties ->
            WateringCanItem(properties.stacksTo(1))
        }

        // Enhancement stone (rare grade, used for Lv4→Lv5 enhancement)
        val ENHANCEMENT_STONE: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "enhancement_stone", Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)
        )

        // Inventory save ticket (preserves inventory on death)
        val INVENTORY_SAVE_TICKET: DeferredItem<Item> = ITEMS.registerSimpleItem(
            "inventory_save_ticket", Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)
        )

        // Wild portal blocks
        private fun wildPortalProperties() = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
            .noOcclusion()
            .lightLevel { 10 }

        private fun returnPortalProperties() = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(-1.0f, 3600000.0f)
            .noLootTable()
            .noOcclusion()
            .lightLevel { 8 }

        val WILD_PORTAL: DeferredBlock<Block> = BLOCKS.registerBlock("wild_portal",
            ::WildPortalBlock, wildPortalProperties())
        val WILD_PORTAL_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("wild_portal", WILD_PORTAL)

        val WILD_PORTAL_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("wild_portal_dummy",
            ::PortalDummyBlock, wildPortalProperties())

        val RETURN_PORTAL: DeferredBlock<Block> = BLOCKS.registerBlock("return_portal",
            ::ReturnPortalBlock, returnPortalProperties())
        val RETURN_PORTAL_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("return_portal", RETURN_PORTAL)

        val RETURN_PORTAL_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("return_portal_dummy",
            ::PortalDummyBlock, returnPortalProperties())

        // Quest reward food - Hunter's Pot
        val HUNTERS_POT: DeferredItem<Item> = ITEMS.registerSimpleItem("hunters_pot",
            Item.Properties()
                .food(FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).build()))

        // Draw tickets (gacha)
        val DRAW_TICKET_NORMAL: DeferredItem<Item> = ITEMS.registerItem("draw_ticket_normal") { properties ->
            GachaTicketItem(properties)
        }
        val DRAW_TICKET_FINE: DeferredItem<Item> = ITEMS.registerItem("draw_ticket_fine") { properties ->
            GachaTicketItem(properties)
        }
        val DRAW_TICKET_RARE: DeferredItem<Item> = ITEMS.registerItem("draw_ticket_rare") { properties ->
            GachaTicketItem(properties)
        }
        val PET_DRAW_TICKET_NORMAL: DeferredItem<Item> = ITEMS.registerItem("pet_draw_ticket_normal") { properties ->
            GachaTicketItem(properties)
        }
        val FURNITURE_DRAW_TICKET_NORMAL: DeferredItem<Item> = ITEMS.registerItem("furniture_draw_ticket_normal") { properties ->
            GachaTicketItem(properties)
        }
        val COSMETIC_DRAW_TICKET_NORMAL: DeferredItem<Item> = ITEMS.registerItem("cosmetic_draw_ticket_normal") { properties ->
            GachaTicketItem(properties)
        }

        // Pet tokens
        val PET_TOKEN_CAT_COMMON: DeferredItem<Item> = ITEMS.registerItem("pet_token_cat_common") { properties ->
            PetTokenItem(PetType.CAT_COMMON, properties)
        }
        val PET_TOKEN_DOG_COMMON: DeferredItem<Item> = ITEMS.registerItem("pet_token_dog_common") { properties ->
            PetTokenItem(PetType.DOG_COMMON, properties)
        }
        val PET_TOKEN_RABBIT_COMMON: DeferredItem<Item> = ITEMS.registerItem("pet_token_rabbit_common") { properties ->
            PetTokenItem(PetType.RABBIT_COMMON, properties)
        }
        val PET_TOKEN_FOX_COMMON: DeferredItem<Item> = ITEMS.registerItem("pet_token_fox_common") { properties ->
            PetTokenItem(PetType.FOX_COMMON, properties)
        }

        // Cosmetic tokens
        val COSMETIC_TOKEN_CAT_EARS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_cat_ears") { properties ->
            CosmeticTokenItem("cat_ears", properties)
        }
        val COSMETIC_TOKEN_CAT_HOODIE: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_cat_hoodie") { properties ->
            CosmeticTokenItem("cat_hoodie", properties)
        }
        val COSMETIC_TOKEN_CAT_PANTS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_cat_pants") { properties ->
            CosmeticTokenItem("cat_pants", properties)
        }
        val COSMETIC_TOKEN_CAT_PAWS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_cat_paws") { properties ->
            CosmeticTokenItem("cat_paws", properties)
        }
        // Dog cosmetic tokens
        val COSMETIC_TOKEN_DOG_EARS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_dog_ears") { properties ->
            CosmeticTokenItem("dog_ears", properties)
        }
        val COSMETIC_TOKEN_DOG_HOODIE: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_dog_hoodie") { properties ->
            CosmeticTokenItem("dog_hoodie", properties)
        }
        val COSMETIC_TOKEN_DOG_PANTS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_dog_pants") { properties ->
            CosmeticTokenItem("dog_pants", properties)
        }
        val COSMETIC_TOKEN_DOG_PAWS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_dog_paws") { properties ->
            CosmeticTokenItem("dog_paws", properties)
        }
        // Rabbit cosmetic tokens
        val COSMETIC_TOKEN_RABBIT_EARS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_rabbit_ears") { properties ->
            CosmeticTokenItem("rabbit_ears", properties)
        }
        val COSMETIC_TOKEN_RABBIT_HOODIE: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_rabbit_hoodie") { properties ->
            CosmeticTokenItem("rabbit_hoodie", properties)
        }
        val COSMETIC_TOKEN_RABBIT_PANTS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_rabbit_pants") { properties ->
            CosmeticTokenItem("rabbit_pants", properties)
        }
        val COSMETIC_TOKEN_RABBIT_PAWS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_rabbit_paws") { properties ->
            CosmeticTokenItem("rabbit_paws", properties)
        }
        // Fox cosmetic tokens
        val COSMETIC_TOKEN_FOX_EARS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_fox_ears") { properties ->
            CosmeticTokenItem("fox_ears", properties)
        }
        val COSMETIC_TOKEN_FOX_HOODIE: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_fox_hoodie") { properties ->
            CosmeticTokenItem("fox_hoodie", properties)
        }
        val COSMETIC_TOKEN_FOX_PANTS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_fox_pants") { properties ->
            CosmeticTokenItem("fox_pants", properties)
        }
        val COSMETIC_TOKEN_FOX_PAWS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_token_fox_paws") { properties ->
            CosmeticTokenItem("fox_paws", properties)
        }

        // Cosmetic virtual armor items (rendering only, 0 defense)
        val COSMETIC_CAT_HEAD: DeferredItem<Item> = ITEMS.registerItem("cosmetic_cat_head") { properties ->
            net.minecraft.world.item.ArmorItem(
                CosmeticArmorItems.COSMETIC_CAT_MATERIAL,
                net.minecraft.world.item.equipment.ArmorType.HELMET,
                properties
            )
        }
        val COSMETIC_CAT_CHEST: DeferredItem<Item> = ITEMS.registerItem("cosmetic_cat_chest") { properties ->
            net.minecraft.world.item.ArmorItem(
                CosmeticArmorItems.COSMETIC_CAT_MATERIAL,
                net.minecraft.world.item.equipment.ArmorType.CHESTPLATE,
                properties
            )
        }
        val COSMETIC_CAT_LEGS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_cat_legs") { properties ->
            net.minecraft.world.item.ArmorItem(
                CosmeticArmorItems.COSMETIC_CAT_MATERIAL,
                net.minecraft.world.item.equipment.ArmorType.LEGGINGS,
                properties
            )
        }
        val COSMETIC_CAT_FEET: DeferredItem<Item> = ITEMS.registerItem("cosmetic_cat_feet") { properties ->
            net.minecraft.world.item.ArmorItem(
                CosmeticArmorItems.COSMETIC_CAT_MATERIAL,
                net.minecraft.world.item.equipment.ArmorType.BOOTS,
                properties
            )
        }
        // Dog cosmetic armor
        val COSMETIC_DOG_HEAD: DeferredItem<Item> = ITEMS.registerItem("cosmetic_dog_head") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_DOG_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET, properties)
        }
        val COSMETIC_DOG_CHEST: DeferredItem<Item> = ITEMS.registerItem("cosmetic_dog_chest") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_DOG_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, properties)
        }
        val COSMETIC_DOG_LEGS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_dog_legs") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_DOG_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS, properties)
        }
        val COSMETIC_DOG_FEET: DeferredItem<Item> = ITEMS.registerItem("cosmetic_dog_feet") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_DOG_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS, properties)
        }
        // Rabbit cosmetic armor
        val COSMETIC_RABBIT_HEAD: DeferredItem<Item> = ITEMS.registerItem("cosmetic_rabbit_head") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_RABBIT_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET, properties)
        }
        val COSMETIC_RABBIT_CHEST: DeferredItem<Item> = ITEMS.registerItem("cosmetic_rabbit_chest") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_RABBIT_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, properties)
        }
        val COSMETIC_RABBIT_LEGS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_rabbit_legs") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_RABBIT_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS, properties)
        }
        val COSMETIC_RABBIT_FEET: DeferredItem<Item> = ITEMS.registerItem("cosmetic_rabbit_feet") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_RABBIT_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS, properties)
        }
        // Fox cosmetic armor
        val COSMETIC_FOX_HEAD: DeferredItem<Item> = ITEMS.registerItem("cosmetic_fox_head") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_FOX_MATERIAL, net.minecraft.world.item.equipment.ArmorType.HELMET, properties)
        }
        val COSMETIC_FOX_CHEST: DeferredItem<Item> = ITEMS.registerItem("cosmetic_fox_chest") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_FOX_MATERIAL, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE, properties)
        }
        val COSMETIC_FOX_LEGS: DeferredItem<Item> = ITEMS.registerItem("cosmetic_fox_legs") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_FOX_MATERIAL, net.minecraft.world.item.equipment.ArmorType.LEGGINGS, properties)
        }
        val COSMETIC_FOX_FEET: DeferredItem<Item> = ITEMS.registerItem("cosmetic_fox_feet") { properties ->
            net.minecraft.world.item.ArmorItem(CosmeticArmorItems.COSMETIC_FOX_MATERIAL, net.minecraft.world.item.equipment.ArmorType.BOOTS, properties)
        }

        // Quest board
        private fun questBoardProperties(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
            .strength(2.5f, 6.0f)
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .noOcclusion()

        val QUEST_BOARD: DeferredBlock<Block> = BLOCKS.registerBlock("quest_board",
            { properties -> com.juyoung.estherserver.quest.QuestBoardBlock(properties) },
            questBoardProperties())
        val QUEST_BOARD_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("quest_board", QUEST_BOARD)

        val QUEST_BOARD_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("quest_board_dummy",
            { properties -> com.juyoung.estherserver.quest.QuestBoardDummyBlock(properties) },
            questBoardProperties().noLootTable())

        // Furniture - Cat sofa
        private fun catSofaProperties(): BlockBehaviour.Properties = BlockBehaviour.Properties.of()
            .strength(2.0f, 6.0f)
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .noOcclusion()

        val CAT_SOFA: DeferredBlock<Block> = BLOCKS.registerBlock("cat_sofa",
            { properties -> CatSofaBlock(properties) },
            catSofaProperties())
        val CAT_SOFA_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("cat_sofa", CAT_SOFA)

        val CAT_SOFA_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("cat_sofa_dummy",
            { properties -> CatSofaDummyBlock(properties) },
            catSofaProperties().noLootTable())

        // Furniture - Dog sofa
        val DOG_SOFA: DeferredBlock<Block> = BLOCKS.registerBlock("dog_sofa",
            { properties -> DogSofaBlock(properties) },
            catSofaProperties())
        val DOG_SOFA_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("dog_sofa", DOG_SOFA)
        val DOG_SOFA_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("dog_sofa_dummy",
            { properties -> DogSofaDummyBlock(properties) },
            catSofaProperties().noLootTable())

        // Furniture - Rabbit sofa
        val RABBIT_SOFA: DeferredBlock<Block> = BLOCKS.registerBlock("rabbit_sofa",
            { properties -> RabbitSofaBlock(properties) },
            catSofaProperties())
        val RABBIT_SOFA_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("rabbit_sofa", RABBIT_SOFA)
        val RABBIT_SOFA_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("rabbit_sofa_dummy",
            { properties -> RabbitSofaDummyBlock(properties) },
            catSofaProperties().noLootTable())

        // Furniture - Fox sofa
        val FOX_SOFA: DeferredBlock<Block> = BLOCKS.registerBlock("fox_sofa",
            { properties -> FoxSofaBlock(properties) },
            catSofaProperties())
        val FOX_SOFA_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("fox_sofa", FOX_SOFA)
        val FOX_SOFA_DUMMY: DeferredBlock<Block> = BLOCKS.registerBlock("fox_sofa_dummy",
            { properties -> FoxSofaDummyBlock(properties) },
            catSofaProperties().noLootTable())

        // Creative tab
        val ESTHER_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("esther_tab",
            Supplier {
                CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.estherserver"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon { COOKING_STATION.get().asItem().defaultInstance }
                    .displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                        // Crops
                        output.accept(RICE_SEEDS.get())
                        output.accept(RICE.get())
                        output.accept(COOKED_RICE.get())
                        output.accept(RED_PEPPER_SEEDS.get())
                        output.accept(RED_PEPPER.get())
                        output.accept(SPINACH_SEEDS.get())
                        output.accept(SPINACH.get())
                        // New crops
                        output.accept(GREEN_ONION_SEEDS.get())
                        output.accept(GREEN_ONION.get())
                        output.accept(GARLIC_SEEDS.get())
                        output.accept(GARLIC.get())
                        output.accept(CABBAGE_SEEDS.get())
                        output.accept(CABBAGE.get())
                        output.accept(SOYBEAN_SEEDS.get())
                        output.accept(SOYBEAN.get())
                        output.accept(SESAME_SEEDS.get())
                        output.accept(SESAME.get())
                        output.accept(GINGER_SEEDS.get())
                        output.accept(GINGER.get())
                        output.accept(PERILLA_SEEDS.get())
                        output.accept(PERILLA.get())
                        output.accept(LOTUS_ROOT_SEEDS.get())
                        output.accept(LOTUS_ROOT.get())
                        output.accept(SHIITAKE_SEEDS.get())
                        output.accept(SHIITAKE.get())
                        output.accept(BAMBOO_SHOOT_SEEDS.get())
                        output.accept(BAMBOO_SHOOT.get())
                        output.accept(WASABI_SEEDS.get())
                        output.accept(WASABI.get())
                        output.accept(GINSENG_SEEDS.get())
                        output.accept(GINSENG.get())
                        output.accept(TRUFFLE_SEEDS.get())
                        output.accept(TRUFFLE.get())
                        output.accept(SAFFRON_SEEDS.get())
                        output.accept(SAFFRON.get())
                        output.accept(MATSUTAKE_SEEDS.get())
                        output.accept(MATSUTAKE.get())
                        output.accept(YUZU_SEEDS.get())
                        output.accept(YUZU.get())
                        output.accept(GREEN_TEA_SEEDS.get())
                        output.accept(GREEN_TEA.get())
                        // Fish
                        output.accept(CRUCIAN_CARP.get())
                        output.accept(SWEETFISH.get())
                        output.accept(MACKEREL.get())
                        output.accept(SQUID_CATCH.get())
                        output.accept(ANCHOVY.get())
                        output.accept(SHRIMP.get())
                        output.accept(CLAM.get())
                        output.accept(SALMON_CATCH.get())
                        output.accept(SEA_BREAM.get())
                        output.accept(EEL.get())
                        output.accept(OCTOPUS.get())
                        output.accept(HAIRTAIL.get())
                        output.accept(YELLOWTAIL.get())
                        output.accept(BLUEFIN_TUNA.get())
                        output.accept(BLOWFISH.get())
                        output.accept(ABALONE.get())
                        output.accept(KING_CRAB.get())
                        output.accept(SEA_URCHIN.get())
                        output.accept(STURGEON.get())
                        // Minerals - Common
                        output.accept(TIN_ORE.get())
                        output.accept(DEEPSLATE_TIN_ORE.get())
                        output.accept(TIN_ORE_RAW.get())
                        output.accept(TIN_INGOT.get())
                        output.accept(ZINC_ORE.get())
                        output.accept(DEEPSLATE_ZINC_ORE.get())
                        output.accept(ZINC_ORE_RAW.get())
                        output.accept(ZINC_INGOT.get())
                        output.accept(JADE_ORE.get())
                        output.accept(DEEPSLATE_JADE_ORE.get())
                        output.accept(JADE_RAW.get())
                        // Minerals - Advanced
                        output.accept(SILVER_ORE.get())
                        output.accept(DEEPSLATE_SILVER_ORE.get())
                        output.accept(SILVER_ORE_RAW.get())
                        output.accept(SILVER_INGOT.get())
                        output.accept(RUBY_ORE.get())
                        output.accept(DEEPSLATE_RUBY_ORE.get())
                        output.accept(RUBY_RAW.get())
                        output.accept(SAPPHIRE_ORE.get())
                        output.accept(DEEPSLATE_SAPPHIRE_ORE.get())
                        output.accept(SAPPHIRE_RAW.get())
                        output.accept(TITANIUM_ORE.get())
                        output.accept(DEEPSLATE_TITANIUM_ORE.get())
                        output.accept(TITANIUM_ORE_RAW.get())
                        output.accept(TITANIUM_INGOT.get())
                        // Minerals - Rare
                        output.accept(PLATINUM_ORE.get())
                        output.accept(DEEPSLATE_PLATINUM_ORE.get())
                        output.accept(PLATINUM_ORE_RAW.get())
                        output.accept(PLATINUM_INGOT.get())
                        output.accept(OPAL_ORE.get())
                        output.accept(DEEPSLATE_OPAL_ORE.get())
                        output.accept(OPAL_RAW.get())
                        output.accept(TANZANITE_ORE.get())
                        output.accept(DEEPSLATE_TANZANITE_ORE.get())
                        output.accept(TANZANITE_RAW.get())
                        output.accept(OBSIDIAN_SHARD.get())
                        // Cooking ingredients
                        output.accept(SEAWEED.get())
                        output.accept(NOODLES.get())
                        // Cooking dishes
                        output.accept(COOKING_STATION.get())
                        output.accept(SPINACH_BIBIMBAP.get())
                        output.accept(FISH_STEW.get())
                        output.accept(GIMBAP.get())
                        output.accept(KIMCHI.get())
                        output.accept(KIMCHI_STEW.get())
                        output.accept(MISO_SOUP.get())
                        output.accept(GRILLED_MACKEREL.get())
                        output.accept(EGG_RICE.get())
                        output.accept(SASHIMI_PLATTER.get())
                        output.accept(EEL_RICE.get())
                        output.accept(DUMPLING.get())
                        output.accept(JAPCHAE.get())
                        output.accept(RAMEN.get())
                        output.accept(MAPO_TOFU.get())
                        output.accept(SEAFOOD_PANCAKE.get())
                        output.accept(LOTUS_SALAD.get())
                        output.accept(GINSENG_CHICKEN.get())
                        output.accept(TRUFFLE_RISOTTO.get())
                        output.accept(BLOWFISH_SASHIMI.get())
                        output.accept(ROYAL_BIBIMBAP.get())
                        output.accept(MATSUTAKE_SOUP.get())
                        output.accept(SAFFRON_RICE.get())
                        output.accept(ABALONE_PORRIDGE.get())
                        output.accept(KING_CRAB_STEW.get())
                        // Utility
                        output.accept(COLLECTION_PEDESTAL.get())
                        output.accept(LAND_DEED.get())
                        output.accept(SPECIAL_FISHING_ROD.get())
                        output.accept(SPECIAL_HOE.get())
                        output.accept(SPECIAL_PICKAXE.get())
                        output.accept(SPECIAL_COOKING_TOOL.get())
                        output.accept(ENHANCEMENT_STONE.get())
                        output.accept(INVENTORY_SAVE_TICKET.get())
                        output.accept(SPECIAL_FARMLAND.get())
                        output.accept(SPRAYER.get())
                        output.accept(WATERING_CAN.get())
                        output.accept(WILD_PORTAL.get())
                        output.accept(RETURN_PORTAL.get())
                        // Quest
                        output.accept(HUNTERS_POT.get())
                        output.accept(DRAW_TICKET_NORMAL.get())
                        output.accept(DRAW_TICKET_FINE.get())
                        output.accept(DRAW_TICKET_RARE.get())
                        output.accept(PET_DRAW_TICKET_NORMAL.get())
                        output.accept(FURNITURE_DRAW_TICKET_NORMAL.get())
                        output.accept(COSMETIC_DRAW_TICKET_NORMAL.get())
                        output.accept(QUEST_BOARD_ITEM.get())
                        // Furniture
                        output.accept(CAT_SOFA_ITEM.get())
                        output.accept(DOG_SOFA_ITEM.get())
                        output.accept(RABBIT_SOFA_ITEM.get())
                        output.accept(FOX_SOFA_ITEM.get())
                        // Pet tokens
                        output.accept(PET_TOKEN_DOG_COMMON.get())
                        output.accept(PET_TOKEN_RABBIT_COMMON.get())
                        output.accept(PET_TOKEN_FOX_COMMON.get())
                    }.build()
            })
    }

    init {
        modEventBus.addListener(::commonSetup)
        modEventBus.addListener(::registerPayloads)
        modEventBus.addListener(::registerEntityAttributes)

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
        ModProfession.ATTACHMENT_TYPES.register(modEventBus)
        ModInventory.ATTACHMENT_TYPES.register(modEventBus)
        ModInventory.MENU_TYPES.register(modEventBus)
        ModWild.ATTACHMENT_TYPES.register(modEventBus)
        ModWild.BLOCK_ENTITY_TYPES.register(modEventBus)
        ModQuest.ATTACHMENT_TYPES.register(modEventBus)
        ModQuest.BLOCK_ENTITY_TYPES.register(modEventBus)
        ModFurniture.BLOCK_ENTITY_TYPES.register(modEventBus)
        ModPets.ATTACHMENT_TYPES.register(modEventBus)
        ModCosmetics.ATTACHMENT_TYPES.register(modEventBus)

        NeoForge.EVENT_BUS.register(this)
        NeoForge.EVENT_BUS.register(SleepHandler)
        NeoForge.EVENT_BUS.register(DaylightHandler)
        NeoForge.EVENT_BUS.register(SitHandler)
        NeoForge.EVENT_BUS.register(CollectionHandler)
        NeoForge.EVENT_BUS.register(ChatTitleHandler)
        NeoForge.EVENT_BUS.register(ClaimProtectionHandler)
        NeoForge.EVENT_BUS.register(EconomyHandler)
        NeoForge.EVENT_BUS.register(ProfessionHandler)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.inventory.InventorySaveHandler)
        NeoForge.EVENT_BUS.register(ProfessionInventoryHandler)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.profession.OreVeinDetector)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.item.AutoFishHandler)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.redstone.RedstoneBlockHandler)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.profession.CropGradeHandler)
        NeoForge.EVENT_BUS.register(com.juyoung.estherserver.wild.OverworldProtectionHandler)
        NeoForge.EVENT_BUS.register(QuestHandler)
        NeoForge.EVENT_BUS.register(CosmeticHandler)
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
            .playToServer(RewardClaimPayload.TYPE, RewardClaimPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    CollectionHandler.handleRewardClaim(player, payload.milestoneId)
                }
            }
            .playToClient(BalanceSyncPayload.TYPE, BalanceSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    EconomyClientHandler.handleSync(payload)
                }
            }
            .playToClient(ProfessionSyncPayload.TYPE, ProfessionSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    ProfessionClientHandler.handleSync(payload)
                }
            }
            .playToClient(OpenShopPayload.TYPE, OpenShopPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    ShopClientHandler.handleOpenShop(payload)
                }
            }
            .playToServer(BuyItemPayload.TYPE, BuyItemPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    ShopBuyRegistry.handleBuy(player, payload.itemId, payload.quantity)
                }
            }
            .playToServer(SellItemPayload.TYPE, SellItemPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    val entity = player.level().getEntity(payload.entityId)
                    val merchant = entity as? MerchantEntity ?: return@enqueueWork
                    ShopBuyRegistry.handleSell(player, payload.slotIndex, payload.quantity, merchant.merchantType)
                }
            }
            .playToServer(EnhanceItemPayload.TYPE, EnhanceItemPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    when (payload.action) {
                        "BUY" -> EnhancementHandler.handleBuyEquipment(player, payload.profession)
                        "ENHANCE" -> EnhancementHandler.handleEnhance(player, payload.profession)
                    }
                }
            }
            .playToClient(ProfessionInventoryPayload.SyncPayload.TYPE, ProfessionInventoryPayload.SyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    ProfessionInventoryClientHandler.handleSync(payload)
                }
            }
            .playToServer(ProfessionInventoryPayload.OpenPayload.TYPE, ProfessionInventoryPayload.OpenPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    ProfessionInventoryHandler.syncToClient(player)
                    player.openMenu(object : net.minecraft.world.MenuProvider {
                        override fun getDisplayName() = Component.translatable("gui.estherserver.prof_inventory.title")
                        override fun createMenu(containerId: Int, inv: net.minecraft.world.entity.player.Inventory, p: net.minecraft.world.entity.player.Player) =
                            ProfessionInventoryMenu(containerId, inv)
                    })
                    // Sync initial tab data (tab 0 = MINING) to client
                    val menu = player.containerMenu as? ProfessionInventoryMenu
                    if (menu != null) {
                        PacketDistributor.sendToPlayer(player, ProfessionInventoryPayload.TabSyncPayload(menu.currentTab, menu.unlockedSlots))
                    }
                }
            }
            .playToServer(ProfessionInventoryPayload.TabSwitchPayload.TYPE, ProfessionInventoryPayload.TabSwitchPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    val menu = player.containerMenu as? ProfessionInventoryMenu ?: return@enqueueWork
                    menu.switchTab(payload.tabIndex)
                }
            }
            .playToClient(ProfessionInventoryPayload.TabSyncPayload.TYPE, ProfessionInventoryPayload.TabSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    ProfessionInventoryClientHandler.handleTabSync(payload)
                }
            }
            .playToClient(QuestSyncPayload.TYPE, QuestSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    QuestClientHandler.handleSync(payload)
                }
            }
            .playToServer(QuestClaimPayload.TYPE, QuestClaimPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    QuestHandler.handleClaimQuest(player, payload.questIndex, payload.isWeekly)
                }
            }
            .playToServer(QuestBonusClaimPayload.TYPE, QuestBonusClaimPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    QuestHandler.handleBonusClaim(player, payload.isWeekly)
                }
            }
            .playToClient(QuestOpenScreenPayload.TYPE, QuestOpenScreenPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    QuestClientHandler.cachedData = payload.data
                    Minecraft.getInstance().setScreen(QuestScreen())
                }
            }
            .playToServer(RequestPetStoragePayload.TYPE, RequestPetStoragePayload.STREAM_CODEC) { _, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    PetHandler.handleRequestStorage(player)
                }
            }
            .playToClient(PetStorageSyncPayload.TYPE, PetStorageSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    PetClientHandler.handleSync(payload)
                    Minecraft.getInstance().setScreen(PetStorageScreen())
                }
            }
            .playToServer(SummonPetPayload.TYPE, SummonPetPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    PetHandler.handleSummonPet(player, payload.petName)
                }
            }
            .playToClient(GachaRoulettePayload.TYPE, GachaRoulettePayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    GachaClientHandler.handleRoulettePayload(payload)
                    Minecraft.getInstance().setScreen(GachaRouletteScreen())
                }
            }
            .playToServer(RequestCosmeticsPayload.TYPE, RequestCosmeticsPayload.STREAM_CODEC) { _, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    CosmeticHandler.handleRequest(player)
                }
            }
            .playToClient(CosmeticSyncPayload.TYPE, CosmeticSyncPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    CosmeticClientHandler.handleSync(payload)
                    // GUI가 아직 열려있지 않을 때만 오픈 (장착/해제 시 재오픈 방지)
                    val currentScreen = Minecraft.getInstance().screen
                    if (currentScreen !is CosmeticScreen) {
                        Minecraft.getInstance().setScreen(CosmeticScreen())
                    }
                }
            }
            .playToServer(EquipCosmeticPayload.TYPE, EquipCosmeticPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    val player = context.player() as? net.minecraft.server.level.ServerPlayer ?: return@enqueueWork
                    CosmeticHandler.handleEquip(player, payload.slotName, payload.cosmeticId)
                }
            }
            .playToClient(CosmeticBroadcastPayload.TYPE, CosmeticBroadcastPayload.STREAM_CODEC) { payload, context ->
                context.enqueueWork {
                    CosmeticClientHandler.handleBroadcast(payload)
                }
            }
    }

    private fun registerEntityAttributes(event: net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent) {
        event.put(MERCHANT_ENTITY.get(), MerchantEntity.createAttributes().build())
        event.put(PET_ENTITY.get(), PetEntity.createAttributes().build())
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("Esther Server mod initialized!")
        CollectibleRegistry.init()
        ItemPriceRegistry.init()
        ShopBuyRegistry.init()
        GachaRegistry.init()
        CosmeticRegistry.init()
        // Register cosmetic armor sets for rendering
        CosmeticArmorItems.registerArmorSet("cosmetic_cat", mapOf(
            net.minecraft.world.entity.EquipmentSlot.HEAD to { COSMETIC_CAT_HEAD.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.CHEST to { COSMETIC_CAT_CHEST.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.LEGS to { COSMETIC_CAT_LEGS.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.FEET to { COSMETIC_CAT_FEET.get() as? net.minecraft.world.item.ArmorItem }
        ))
        CosmeticArmorItems.registerArmorSet("cosmetic_dog", mapOf(
            net.minecraft.world.entity.EquipmentSlot.HEAD to { COSMETIC_DOG_HEAD.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.CHEST to { COSMETIC_DOG_CHEST.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.LEGS to { COSMETIC_DOG_LEGS.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.FEET to { COSMETIC_DOG_FEET.get() as? net.minecraft.world.item.ArmorItem }
        ))
        CosmeticArmorItems.registerArmorSet("cosmetic_rabbit", mapOf(
            net.minecraft.world.entity.EquipmentSlot.HEAD to { COSMETIC_RABBIT_HEAD.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.CHEST to { COSMETIC_RABBIT_CHEST.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.LEGS to { COSMETIC_RABBIT_LEGS.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.FEET to { COSMETIC_RABBIT_FEET.get() as? net.minecraft.world.item.ArmorItem }
        ))
        CosmeticArmorItems.registerArmorSet("cosmetic_fox", mapOf(
            net.minecraft.world.entity.EquipmentSlot.HEAD to { COSMETIC_FOX_HEAD.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.CHEST to { COSMETIC_FOX_CHEST.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.LEGS to { COSMETIC_FOX_LEGS.get() as? net.minecraft.world.item.ArmorItem },
            net.minecraft.world.entity.EquipmentSlot.FEET to { COSMETIC_FOX_FEET.get() as? net.minecraft.world.item.ArmorItem }
        ))
        ProfessionHandler.init()
        ProfessionBonusHelper.initOreGrades()
        ProfessionBonusHelper.initContentGrades()
        // QuestPool is initialized via its init block (no explicit call needed)

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
        ShopCommand.register(event.dispatcher)
        ProfessionCommand.register(event.dispatcher)
        WildCommand.register(event.dispatcher)
        QuestCommand.register(event.dispatcher)
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
            event.register(ModKeyBindings.PROFESSION_KEY)
            event.register(ModKeyBindings.PROFESSION_INVENTORY_KEY)
            event.register(ModKeyBindings.TITLE_KEY)
            event.register(ModKeyBindings.PET_KEY)
            event.register(ModKeyBindings.COSMETIC_KEY)
        }

        @SubscribeEvent
        fun onRegisterMenuScreens(event: net.neoforged.neoforge.client.event.RegisterMenuScreensEvent) {
            event.register(ModInventory.PROFESSION_INVENTORY_MENU.get(), ::ProfessionInventoryContainerScreen)
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
            event.registerEntityRenderer(MERCHANT_ENTITY.get()) { context ->
                MerchantEntityRenderer(context)
            }
            event.registerEntityRenderer(PET_ENTITY.get()) { context ->
                PetEntityRenderer(context)
            }
        }

        @SubscribeEvent
        fun onRegisterLayerDefinitions(event: net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions) {
            event.registerLayerDefinition(PetEntityModel.LAYER_LOCATION) { PetEntityModel.createBodyLayer() }
        }
    }

    private fun onItemTooltip(event: ItemTooltipEvent) {
        if (event.toolTip.isEmpty()) return

        // Enhancement level display
        val enhancementLevel = event.itemStack.get(ModDataComponents.ENHANCEMENT_LEVEL.get())
        if (enhancementLevel != null) {
            val gradeColor = when {
                enhancementLevel >= 5 -> net.minecraft.ChatFormatting.BLUE
                enhancementLevel >= 3 -> net.minecraft.ChatFormatting.GREEN
                else -> net.minecraft.ChatFormatting.WHITE
            }
            if (gradeColor != net.minecraft.ChatFormatting.WHITE) {
                event.toolTip[0] = event.toolTip[0].copy().withStyle(gradeColor)
            }
            val gradeKey = EnhancementHandler.getGradeTranslationKey(enhancementLevel)
            event.toolTip.add(1,
                Component.translatable("tooltip.estherserver.enhancement_level", enhancementLevel)
                    .append(" (")
                    .append(Component.translatable(gradeKey))
                    .append(")")
                    .withStyle(gradeColor))
        }
    }

    @SubscribeEvent
    fun onBreakSpeed(event: PlayerEvent.BreakSpeed) {
        val stack = event.entity.mainHandItem
        if (stack.item === SPECIAL_PICKAXE.get() && event.state.`is`(BlockTags.MINEABLE_WITH_PICKAXE)) {
            val enhLevel = stack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)

            // Check if pickaxe enhancement level is sufficient for this block's tier
            val blockState = event.state
            val canMine = when {
                blockState.`is`(BlockTags.NEEDS_DIAMOND_TOOL) -> enhLevel >= 3
                blockState.`is`(BlockTags.NEEDS_IRON_TOOL) -> enhLevel >= 2
                blockState.`is`(BlockTags.NEEDS_STONE_TOOL) -> enhLevel >= 1
                else -> true  // wood-tier blocks always mineable
            }

            if (!canMine) {
                event.newSpeed = 0f
                return
            }

            // Enhancement level determines base mining speed (matches vanilla pickaxe tiers)
            val tierSpeed = when {
                enhLevel >= 3 -> 8.0f  // diamond
                enhLevel >= 2 -> 6.0f  // iron
                enhLevel >= 1 -> 4.0f  // stone
                else -> 2.0f           // wood
            }

            // Profession level mining speed bonus (+1% per level)
            val profLevel = when {
                event.entity is net.minecraft.server.level.ServerPlayer ->
                    ProfessionHandler.getLevel(event.entity as net.minecraft.server.level.ServerPlayer, com.juyoung.estherserver.profession.Profession.MINING)
                event.entity.level().isClientSide ->
                    ProfessionClientHandler.cachedData.getLevel(com.juyoung.estherserver.profession.Profession.MINING)
                else -> 0
            }
            val profBonus = ProfessionBonusHelper.getMiningSpeedBonus(profLevel)
            event.newSpeed = tierSpeed * (1.0f + profBonus)
        }
    }

    @SubscribeEvent
    fun onCropGrowPost(event: CropGrowEvent.Post) {
        val serverLevel = event.level as? net.minecraft.server.level.ServerLevel ?: return
        val pos = event.pos
        val currentState = serverLevel.getBlockState(pos)
        val block = currentState.block as? CustomCropBlock ?: return

        val chunkPos = net.minecraft.world.level.ChunkPos(pos)
        val claim = ChunkClaimManager.getClaimInfo(serverLevel, chunkPos) ?: return
        val owner = serverLevel.server?.playerList?.getPlayer(claim.ownerUUID) ?: return

        val farmingLevel = ProfessionHandler.getLevel(owner, com.juyoung.estherserver.profession.Profession.FARMING)
        if (farmingLevel <= 0) return

        if (serverLevel.random.nextFloat() < farmingLevel * 0.01f) {
            val ageProperty = net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7
            val currentAge: Int = currentState.getValue(ageProperty)
            if (currentAge < 7) {
                serverLevel.setBlock(pos, block.getStateForAge(currentAge + 1), 2)
            }
        }
    }

    @SubscribeEvent
    fun onPlayerLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        val player = event.entity as? net.minecraft.server.level.ServerPlayer ?: return
        val vehicle = player.vehicle
        if (vehicle is PetEntity) {
            val safeX = player.x
            val safeZ = player.z
            // 펫의 Y 위치(지면)를 기준으로 안전한 높이 확보
            val safeY = vehicle.y
            player.stopRiding() // removePassenger → pet discard
            player.teleportTo(safeX, safeY, safeZ)
        }
    }

    @SubscribeEvent
    fun onCropGrow(event: CropGrowEvent.Pre) {
        val belowState = event.level.getBlockState(event.pos.below())
        if (belowState.block is SpecialFarmlandBlock) {
            if (belowState.getValue(SpecialFarmlandBlock.MOISTURE) == 0) {
                event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW)
            }
        }
    }

    @SubscribeEvent
    fun onItemPickup(event: net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre) {
        val player = event.player as? net.minecraft.server.level.ServerPlayer ?: return
        if (player.containerMenu is ProfessionInventoryMenu) return
        if (event.itemEntity.hasPickUpDelay()) return
        val stack = event.itemEntity.item
        if (stack.isEmpty) return

        val countBefore = stack.count
        if (ProfessionInventoryHandler.tryAddItem(player, stack)) {
            val pickedUp = countBefore - stack.count
            // 바닐라와 동일한 픽업 애니메이션 (아이템이 플레이어에게 날아감)
            player.take(event.itemEntity, pickedUp)
            // 바닐라와 동일한 픽업 사운드
            player.level().playSound(
                null, player.x, player.y, player.z,
                net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.2f,
                ((player.random.nextFloat() - player.random.nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
            if (stack.isEmpty) {
                event.itemEntity.discard()
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE)
            }
            ProfessionInventoryHandler.syncToClient(player)
        }
    }
}
