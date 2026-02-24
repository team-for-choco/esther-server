package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem

object ProfessionHandler {

    private val itemProfessionMap = mutableMapOf<ResourceLocation, Profession>()
    private val vanillaMiningXpMap = mutableMapOf<ResourceLocation, Int>()
    private val cropSeedMap = mutableMapOf<ResourceLocation, DeferredItem<*>>()

    fun init() {
        // Fishing (19 fish species)
        register(EstherServerMod.CRUCIAN_CARP, Profession.FISHING)
        register(EstherServerMod.SWEETFISH, Profession.FISHING)
        register(EstherServerMod.MACKEREL, Profession.FISHING)
        register(EstherServerMod.SQUID_CATCH, Profession.FISHING)
        register(EstherServerMod.ANCHOVY, Profession.FISHING)
        register(EstherServerMod.SHRIMP, Profession.FISHING)
        register(EstherServerMod.CLAM, Profession.FISHING)
        register(EstherServerMod.SALMON_CATCH, Profession.FISHING)
        register(EstherServerMod.SEA_BREAM, Profession.FISHING)
        register(EstherServerMod.EEL, Profession.FISHING)
        register(EstherServerMod.OCTOPUS, Profession.FISHING)
        register(EstherServerMod.HAIRTAIL, Profession.FISHING)
        register(EstherServerMod.YELLOWTAIL, Profession.FISHING)
        register(EstherServerMod.BLUEFIN_TUNA, Profession.FISHING)
        register(EstherServerMod.BLOWFISH, Profession.FISHING)
        register(EstherServerMod.ABALONE, Profession.FISHING)
        register(EstherServerMod.KING_CRAB, Profession.FISHING)
        register(EstherServerMod.SEA_URCHIN, Profession.FISHING)
        register(EstherServerMod.STURGEON, Profession.FISHING)

        // Farming (20 crops)
        register(EstherServerMod.RICE, Profession.FARMING)
        register(EstherServerMod.RED_PEPPER, Profession.FARMING)
        register(EstherServerMod.SPINACH, Profession.FARMING)
        register(EstherServerMod.GREEN_ONION, Profession.FARMING)
        register(EstherServerMod.GARLIC, Profession.FARMING)
        register(EstherServerMod.CABBAGE, Profession.FARMING)
        register(EstherServerMod.SOYBEAN, Profession.FARMING)
        register(EstherServerMod.SESAME, Profession.FARMING)
        register(EstherServerMod.GINGER, Profession.FARMING)
        register(EstherServerMod.PERILLA, Profession.FARMING)
        register(EstherServerMod.LOTUS_ROOT, Profession.FARMING)
        register(EstherServerMod.SHIITAKE, Profession.FARMING)
        register(EstherServerMod.BAMBOO_SHOOT, Profession.FARMING)
        register(EstherServerMod.WASABI, Profession.FARMING)
        register(EstherServerMod.GINSENG, Profession.FARMING)
        register(EstherServerMod.TRUFFLE, Profession.FARMING)
        register(EstherServerMod.SAFFRON, Profession.FARMING)
        register(EstherServerMod.MATSUTAKE, Profession.FARMING)
        register(EstherServerMod.YUZU, Profession.FARMING)
        register(EstherServerMod.GREEN_TEA, Profession.FARMING)

        // Crop-to-seed mapping (for seed preservation)
        registerCropSeed(EstherServerMod.RICE_CROP, EstherServerMod.RICE_SEEDS)
        registerCropSeed(EstherServerMod.RED_PEPPER_CROP, EstherServerMod.RED_PEPPER_SEEDS)
        registerCropSeed(EstherServerMod.SPINACH_CROP, EstherServerMod.SPINACH_SEEDS)
        registerCropSeed(EstherServerMod.GREEN_ONION_CROP, EstherServerMod.GREEN_ONION_SEEDS)
        registerCropSeed(EstherServerMod.GARLIC_CROP, EstherServerMod.GARLIC_SEEDS)
        registerCropSeed(EstherServerMod.CABBAGE_CROP, EstherServerMod.CABBAGE_SEEDS)
        registerCropSeed(EstherServerMod.SOYBEAN_CROP, EstherServerMod.SOYBEAN_SEEDS)
        registerCropSeed(EstherServerMod.SESAME_CROP, EstherServerMod.SESAME_SEEDS)
        registerCropSeed(EstherServerMod.GINGER_CROP, EstherServerMod.GINGER_SEEDS)
        registerCropSeed(EstherServerMod.PERILLA_CROP, EstherServerMod.PERILLA_SEEDS)
        registerCropSeed(EstherServerMod.LOTUS_ROOT_CROP, EstherServerMod.LOTUS_ROOT_SEEDS)
        registerCropSeed(EstherServerMod.SHIITAKE_CROP, EstherServerMod.SHIITAKE_SEEDS)
        registerCropSeed(EstherServerMod.BAMBOO_SHOOT_CROP, EstherServerMod.BAMBOO_SHOOT_SEEDS)
        registerCropSeed(EstherServerMod.WASABI_CROP, EstherServerMod.WASABI_SEEDS)
        registerCropSeed(EstherServerMod.GINSENG_CROP, EstherServerMod.GINSENG_SEEDS)
        registerCropSeed(EstherServerMod.TRUFFLE_CROP, EstherServerMod.TRUFFLE_SEEDS)
        registerCropSeed(EstherServerMod.SAFFRON_CROP, EstherServerMod.SAFFRON_SEEDS)
        registerCropSeed(EstherServerMod.MATSUTAKE_CROP, EstherServerMod.MATSUTAKE_SEEDS)
        registerCropSeed(EstherServerMod.YUZU_CROP, EstherServerMod.YUZU_SEEDS)
        registerCropSeed(EstherServerMod.GREEN_TEA_CROP, EstherServerMod.GREEN_TEA_SEEDS)

        // Mining (custom ores → raw items)
        register(EstherServerMod.TIN_ORE_RAW, Profession.MINING)
        register(EstherServerMod.ZINC_ORE_RAW, Profession.MINING)
        register(EstherServerMod.JADE_RAW, Profession.MINING)
        register(EstherServerMod.SILVER_ORE_RAW, Profession.MINING)
        register(EstherServerMod.RUBY_RAW, Profession.MINING)
        register(EstherServerMod.SAPPHIRE_RAW, Profession.MINING)
        register(EstherServerMod.TITANIUM_ORE_RAW, Profession.MINING)
        register(EstherServerMod.PLATINUM_ORE_RAW, Profession.MINING)
        register(EstherServerMod.OPAL_RAW, Profession.MINING)
        register(EstherServerMod.TANZANITE_RAW, Profession.MINING)
        register(EstherServerMod.OBSIDIAN_SHARD, Profession.MINING)

        // Vanilla mining XP (fixed amount per ore type, no quality)
        registerVanillaMining("minecraft:coal", 1)
        registerVanillaMining("minecraft:raw_copper", 1)
        registerVanillaMining("minecraft:redstone", 1)
        registerVanillaMining("minecraft:lapis_lazuli", 2)
        registerVanillaMining("minecraft:raw_iron", 2)
        registerVanillaMining("minecraft:quartz", 2)
        registerVanillaMining("minecraft:amethyst_shard", 2)
        registerVanillaMining("minecraft:raw_gold", 3)
        registerVanillaMining("minecraft:emerald", 4)
        registerVanillaMining("minecraft:diamond", 5)
    }

    private fun register(item: DeferredItem<*>, profession: Profession) {
        itemProfessionMap[item.id] = profession
    }

    fun registerItemProfession(itemId: String, profession: Profession) {
        itemProfessionMap[ResourceLocation.parse(itemId)] = profession
    }

    private fun registerCropSeed(crop: DeferredBlock<Block>, seed: DeferredItem<*>) {
        cropSeedMap[crop.id] = seed
    }

    fun registerCropSeedMapping(cropId: ResourceLocation, seed: DeferredItem<*>) {
        cropSeedMap[cropId] = seed
    }

    private fun registerVanillaMining(item: String, xp: Int) {
        vanillaMiningXpMap[ResourceLocation.parse(item)] = xp
    }

    fun getVanillaMiningXp(stack: ItemStack): Int? {
        val key = BuiltInRegistries.ITEM.getKey(stack.item)
        return vanillaMiningXpMap[key]
    }

    fun getProfessionForItem(stack: ItemStack): Profession? {
        val key = BuiltInRegistries.ITEM.getKey(stack.item)
        return itemProfessionMap[key]
    }

    fun getSeedForCrop(blockId: ResourceLocation): ItemStack? {
        val seed = cropSeedMap[blockId] ?: return null
        return ItemStack(seed.get())
    }

    fun addExperience(player: ServerPlayer, profession: Profession, amount: Int) {
        val data = player.getData(ModProfession.PROFESSION_DATA.get())
        val currentLevel = data.getLevel(profession)

        if (currentLevel >= Profession.MAX_LEVEL) return

        var currentXp = data.getXp(profession) + amount
        var level = currentLevel
        var leveledUp = false

        while (level < Profession.MAX_LEVEL) {
            val required = Profession.getRequiredXp(level + 1)
            if (currentXp >= required) {
                currentXp -= required
                level++
                leveledUp = true
            } else {
                break
            }
        }

        data.setXp(profession, currentXp)
        data.setLevel(profession, level)
        player.setData(ModProfession.PROFESSION_DATA.get(), data)

        // XP gain message (action bar)
        val requiredForNext = if (level < Profession.MAX_LEVEL) Profession.getRequiredXp(level + 1) else 0
        player.displayClientMessage(
            Component.translatable(
                "message.estherserver.profession_xp",
                Component.translatable(profession.translationKey),
                amount,
                currentXp,
                requiredForNext
            ),
            true
        )

        if (leveledUp) {
            // Level-up chat message
            player.sendSystemMessage(
                Component.translatable(
                    "message.estherserver.profession_levelup",
                    Component.translatable(profession.translationKey),
                    level
                )
            )
            // Level-up sound
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f)
        }

        syncToClient(player)
    }

    fun getLevel(player: ServerPlayer, profession: Profession): Int {
        return player.getData(ModProfession.PROFESSION_DATA.get()).getLevel(profession)
    }

    fun getXp(player: ServerPlayer, profession: Profession): Int {
        return player.getData(ModProfession.PROFESSION_DATA.get()).getXp(profession)
    }

    fun syncToClient(player: ServerPlayer) {
        val data = player.getData(ModProfession.PROFESSION_DATA.get())
        PacketDistributor.sendToPlayer(player, ProfessionSyncPayload(data))
    }

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }

    @SubscribeEvent
    fun onPlayerRespawn(event: PlayerEvent.PlayerRespawnEvent) {
        val player = event.entity as? ServerPlayer ?: return
        syncToClient(player)
    }
}
