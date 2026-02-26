package com.juyoung.estherserver.collection

import com.juyoung.estherserver.EstherServerMod.Companion as Mod
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.*
import net.neoforged.neoforge.registries.DeferredItem

enum class CollectionCategory(val translationKey: String) {
    FISH("gui.estherserver.collection.category.fish"),
    CROPS("gui.estherserver.collection.category.crops"),
    MINERALS("gui.estherserver.collection.category.minerals"),
    COOKING("gui.estherserver.collection.category.cooking"),
    BLOCKS("gui.estherserver.collection.category.blocks"),
    EQUIPMENT("gui.estherserver.collection.category.equipment"),
    FOOD("gui.estherserver.collection.category.food"),
    MATERIALS("gui.estherserver.collection.category.materials")
}

data class CollectibleDefinition(
    val key: CollectionKey,
    val category: CollectionCategory,
    val requiredCount: Int = 1
)

object CollectibleRegistry {
    private val definitions = mutableListOf<CollectibleDefinition>()
    private val keyToDefinition = mutableMapOf<CollectionKey, CollectibleDefinition>()
    private val categoryCache = mutableMapOf<CollectionCategory, List<CollectibleDefinition>>()
    private var initialized = false

    /** Phase A에 등록된 키 (Phase B에서 중복 방지용) */
    private val phaseAKeys = mutableSetOf<CollectionKey>()

    fun init() {
        if (initialized) return
        initialized = true

        registerPhaseA()
        registerPhaseB()

        // 카테고리 캐시 빌드
        categoryCache.clear()
        for (category in CollectionCategory.entries) {
            categoryCache[category] = definitions.filter { it.category == category }
        }
    }

    // ─── Phase A: 커스텀 아이템 명시 등록 ───

    private fun registerPhaseA() {
        // Fish - Common (7)
        register(Mod.CRUCIAN_CARP, CollectionCategory.FISH)
        register(Mod.SWEETFISH, CollectionCategory.FISH)
        register(Mod.MACKEREL, CollectionCategory.FISH)
        register(Mod.SQUID_CATCH, CollectionCategory.FISH)
        register(Mod.ANCHOVY, CollectionCategory.FISH)
        register(Mod.SHRIMP, CollectionCategory.FISH)
        register(Mod.CLAM, CollectionCategory.FISH)
        // Fish - Advanced (6)
        register(Mod.SALMON_CATCH, CollectionCategory.FISH)
        register(Mod.SEA_BREAM, CollectionCategory.FISH)
        register(Mod.EEL, CollectionCategory.FISH)
        register(Mod.OCTOPUS, CollectionCategory.FISH)
        register(Mod.HAIRTAIL, CollectionCategory.FISH)
        register(Mod.YELLOWTAIL, CollectionCategory.FISH)
        // Fish - Rare (6)
        register(Mod.BLUEFIN_TUNA, CollectionCategory.FISH)
        register(Mod.BLOWFISH, CollectionCategory.FISH)
        register(Mod.ABALONE, CollectionCategory.FISH)
        register(Mod.KING_CRAB, CollectionCategory.FISH)
        register(Mod.SEA_URCHIN, CollectionCategory.FISH)
        register(Mod.STURGEON, CollectionCategory.FISH)

        // Crops - seeds (20)
        register(Mod.RICE_SEEDS, CollectionCategory.CROPS)
        register(Mod.RED_PEPPER_SEEDS, CollectionCategory.CROPS)
        register(Mod.SPINACH_SEEDS, CollectionCategory.CROPS)
        register(Mod.GREEN_ONION_SEEDS, CollectionCategory.CROPS)
        register(Mod.GARLIC_SEEDS, CollectionCategory.CROPS)
        register(Mod.CABBAGE_SEEDS, CollectionCategory.CROPS)
        register(Mod.SOYBEAN_SEEDS, CollectionCategory.CROPS)
        register(Mod.SESAME_SEEDS, CollectionCategory.CROPS)
        register(Mod.GINGER_SEEDS, CollectionCategory.CROPS)
        register(Mod.PERILLA_SEEDS, CollectionCategory.CROPS)
        register(Mod.LOTUS_ROOT_SEEDS, CollectionCategory.CROPS)
        register(Mod.SHIITAKE_SEEDS, CollectionCategory.CROPS)
        register(Mod.BAMBOO_SHOOT_SEEDS, CollectionCategory.CROPS)
        register(Mod.WASABI_SEEDS, CollectionCategory.CROPS)
        register(Mod.GINSENG_SEEDS, CollectionCategory.CROPS)
        register(Mod.TRUFFLE_SEEDS, CollectionCategory.CROPS)
        register(Mod.SAFFRON_SEEDS, CollectionCategory.CROPS)
        register(Mod.MATSUTAKE_SEEDS, CollectionCategory.CROPS)
        register(Mod.YUZU_SEEDS, CollectionCategory.CROPS)
        register(Mod.GREEN_TEA_SEEDS, CollectionCategory.CROPS)
        // Crops - produce (21 including cooked_rice)
        register(Mod.RICE, CollectionCategory.CROPS)
        register(Mod.COOKED_RICE, CollectionCategory.CROPS)
        register(Mod.RED_PEPPER, CollectionCategory.CROPS)
        register(Mod.SPINACH, CollectionCategory.CROPS)
        register(Mod.GREEN_ONION, CollectionCategory.CROPS)
        register(Mod.GARLIC, CollectionCategory.CROPS)
        register(Mod.CABBAGE, CollectionCategory.CROPS)
        register(Mod.SOYBEAN, CollectionCategory.CROPS)
        register(Mod.SESAME, CollectionCategory.CROPS)
        register(Mod.GINGER, CollectionCategory.CROPS)
        register(Mod.PERILLA, CollectionCategory.CROPS)
        register(Mod.LOTUS_ROOT, CollectionCategory.CROPS)
        register(Mod.SHIITAKE, CollectionCategory.CROPS)
        register(Mod.BAMBOO_SHOOT, CollectionCategory.CROPS)
        register(Mod.WASABI, CollectionCategory.CROPS)
        register(Mod.GINSENG, CollectionCategory.CROPS)
        register(Mod.TRUFFLE, CollectionCategory.CROPS)
        register(Mod.SAFFRON, CollectionCategory.CROPS)
        register(Mod.MATSUTAKE, CollectionCategory.CROPS)
        register(Mod.YUZU, CollectionCategory.CROPS)
        register(Mod.GREEN_TEA, CollectionCategory.CROPS)

        // Minerals - Common (raw)
        register(Mod.TIN_ORE_RAW, CollectionCategory.MINERALS)
        register(Mod.ZINC_ORE_RAW, CollectionCategory.MINERALS)
        register(Mod.JADE_RAW, CollectionCategory.MINERALS)
        // Minerals - Common (ingot)
        register(Mod.TIN_INGOT, CollectionCategory.MINERALS)
        register(Mod.ZINC_INGOT, CollectionCategory.MINERALS)
        // Minerals - Advanced (raw)
        register(Mod.SILVER_ORE_RAW, CollectionCategory.MINERALS)
        register(Mod.RUBY_RAW, CollectionCategory.MINERALS)
        register(Mod.SAPPHIRE_RAW, CollectionCategory.MINERALS)
        register(Mod.TITANIUM_ORE_RAW, CollectionCategory.MINERALS)
        // Minerals - Advanced (ingot)
        register(Mod.SILVER_INGOT, CollectionCategory.MINERALS)
        register(Mod.TITANIUM_INGOT, CollectionCategory.MINERALS)
        // Minerals - Rare (raw)
        register(Mod.PLATINUM_ORE_RAW, CollectionCategory.MINERALS)
        register(Mod.OPAL_RAW, CollectionCategory.MINERALS)
        register(Mod.TANZANITE_RAW, CollectionCategory.MINERALS)
        // Minerals - Rare (ingot)
        register(Mod.PLATINUM_INGOT, CollectionCategory.MINERALS)
        // Special
        register(Mod.OBSIDIAN_SHARD, CollectionCategory.MINERALS)

        // Cooking - Common (8)
        register(Mod.SPINACH_BIBIMBAP, CollectionCategory.COOKING)
        register(Mod.FISH_STEW, CollectionCategory.COOKING)
        register(Mod.GIMBAP, CollectionCategory.COOKING)
        register(Mod.KIMCHI, CollectionCategory.COOKING)
        register(Mod.KIMCHI_STEW, CollectionCategory.COOKING)
        register(Mod.MISO_SOUP, CollectionCategory.COOKING)
        register(Mod.GRILLED_MACKEREL, CollectionCategory.COOKING)
        register(Mod.EGG_RICE, CollectionCategory.COOKING)
        // Cooking - Advanced (8)
        register(Mod.SASHIMI_PLATTER, CollectionCategory.COOKING)
        register(Mod.EEL_RICE, CollectionCategory.COOKING)
        register(Mod.DUMPLING, CollectionCategory.COOKING)
        register(Mod.JAPCHAE, CollectionCategory.COOKING)
        register(Mod.RAMEN, CollectionCategory.COOKING)
        register(Mod.MAPO_TOFU, CollectionCategory.COOKING)
        register(Mod.SEAFOOD_PANCAKE, CollectionCategory.COOKING)
        register(Mod.LOTUS_SALAD, CollectionCategory.COOKING)
        // Cooking - Rare (8)
        register(Mod.GINSENG_CHICKEN, CollectionCategory.COOKING)
        register(Mod.TRUFFLE_RISOTTO, CollectionCategory.COOKING)
        register(Mod.BLOWFISH_SASHIMI, CollectionCategory.COOKING)
        register(Mod.ROYAL_BIBIMBAP, CollectionCategory.COOKING)
        register(Mod.MATSUTAKE_SOUP, CollectionCategory.COOKING)
        register(Mod.SAFFRON_RICE, CollectionCategory.COOKING)
        register(Mod.ABALONE_PORRIDGE, CollectionCategory.COOKING)
        register(Mod.KING_CRAB_STEW, CollectionCategory.COOKING)
    }

    // ─── Phase B: 바닐라+커스텀 아이템 자동 분류 ───

    private fun registerPhaseB() {
        for (entry in BuiltInRegistries.ITEM) {
            val itemId = BuiltInRegistries.ITEM.getKey(entry)
            val key = CollectionKey(itemId)

            // Phase A에서 이미 등록됨
            if (key in phaseAKeys) continue

            // 제외 대상 클래스
            if (isExcludedItem(entry)) continue

            val category = classifyItem(entry)
            val def = CollectibleDefinition(key, category)
            definitions.add(def)
            keyToDefinition[key] = def
        }
    }

    private val excludedItems by lazy {
        setOf(
            // 관리자 전용
            Items.COMMAND_BLOCK,
            Items.CHAIN_COMMAND_BLOCK,
            Items.REPEATING_COMMAND_BLOCK,
            Items.COMMAND_BLOCK_MINECART,
            Items.BARRIER,
            Items.STRUCTURE_BLOCK,
            Items.STRUCTURE_VOID,
            Items.JIGSAW,
            Items.LIGHT,
            Items.DEBUG_STICK,
            Items.KNOWLEDGE_BOOK,
            Items.BUNDLE,
            // 커스텀 도구/블록 (재획득 불가)
            Mod.SPECIAL_FISHING_ROD.get(),
            Mod.SPECIAL_HOE.get(),
            Mod.SPECIAL_PICKAXE.get(),
            Mod.SPECIAL_COOKING_TOOL.get(),
            Mod.COOKING_STATION_ITEM.get(),
            Mod.COLLECTION_PEDESTAL_ITEM.get(),
            Mod.WILD_PORTAL_ITEM.get(),
            Mod.RETURN_PORTAL_ITEM.get(),
            Mod.QUEST_BOARD_ITEM.get(),
            // 뽑기권 (도감 제외)
            Mod.DRAW_TICKET_NORMAL.get(),
            Mod.DRAW_TICKET_FINE.get(),
            Mod.DRAW_TICKET_RARE.get(),
            Mod.PET_DRAW_TICKET_NORMAL.get(),
            Mod.FURNITURE_DRAW_TICKET_NORMAL.get(),
            // 레드스톤 재료 아이템 (직접)
            Items.REDSTONE,
            Items.REDSTONE_TORCH,
            Items.REPEATER,
            Items.PISTON,
            Items.DISPENSER,
            Items.DROPPER,
            Items.OBSERVER,
            Items.NOTE_BLOCK,
            Items.REDSTONE_LAMP,
            Items.TARGET,
            Items.POWERED_RAIL,
            Items.DETECTOR_RAIL,
            // 레드스톤 재료 아이템 (간접: 레드스톤 횃불/피스톤 경유)
            Items.COMPARATOR,
            Items.STICKY_PISTON,
            Items.ACTIVATOR_RAIL
        )
    }

    private fun isExcludedItem(item: Item): Boolean {
        return item in excludedItems ||
            item is SpawnEggItem ||
            item is GameMasterBlockItem ||
            item is AirItem
    }

    private fun classifyItem(item: Item): CollectionCategory {
        // Equipment: 무기/갑옷/도구
        if (item is SwordItem || item is DiggerItem || item is ArmorItem ||
            item is BowItem || item is CrossbowItem || item is ShieldItem ||
            item is TridentItem || item is MaceItem
        ) {
            return CollectionCategory.EQUIPMENT
        }

        // Food: DataComponents.FOOD 보유
        val defaultStack = item.defaultInstance
        if (defaultStack.has(DataComponents.FOOD)) {
            return CollectionCategory.FOOD
        }

        // Blocks: BlockItem 계열
        if (item is BlockItem) {
            return CollectionCategory.BLOCKS
        }

        // 나머지: Materials
        return CollectionCategory.MATERIALS
    }

    private fun register(item: DeferredItem<*>, category: CollectionCategory, requiredCount: Int = 1) {
        val key = CollectionKey(item.id)
        val def = CollectibleDefinition(key, category, requiredCount)
        definitions.add(def)
        keyToDefinition[key] = def
        phaseAKeys.add(key)
    }

    fun isValidKey(key: CollectionKey): Boolean = keyToDefinition.containsKey(key)

    fun getDefinition(key: CollectionKey): CollectibleDefinition? = keyToDefinition[key]

    fun getRequiredCount(key: CollectionKey): Int = keyToDefinition[key]?.requiredCount ?: 1

    fun getAllDefinitions(): List<CollectibleDefinition> = definitions.toList()

    fun getDefinitionsByCategory(category: CollectionCategory): List<CollectibleDefinition> =
        categoryCache[category] ?: emptyList()

    fun getTotalCount(): Int = definitions.size
}
