package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.quality.ItemQuality
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
import net.neoforged.neoforge.registries.DeferredItem

object ProfessionHandler {

    private val itemProfessionMap = mutableMapOf<ResourceLocation, Profession>()

    fun init() {
        // Fishing
        register(EstherServerMod.TEST_FISH, Profession.FISHING)

        // Farming
        register(EstherServerMod.TEST_HARVEST, Profession.FARMING)
        register(EstherServerMod.RICE, Profession.FARMING)
        register(EstherServerMod.RED_PEPPER, Profession.FARMING)
        register(EstherServerMod.SPINACH, Profession.FARMING)

        // Mining
        register(EstherServerMod.TEST_ORE_RAW, Profession.MINING)
        registerVanilla("minecraft:coal", Profession.MINING)
        registerVanilla("minecraft:raw_iron", Profession.MINING)
        registerVanilla("minecraft:raw_gold", Profession.MINING)
        registerVanilla("minecraft:raw_copper", Profession.MINING)
        registerVanilla("minecraft:diamond", Profession.MINING)
        registerVanilla("minecraft:emerald", Profession.MINING)
        registerVanilla("minecraft:lapis_lazuli", Profession.MINING)
        registerVanilla("minecraft:redstone", Profession.MINING)
        registerVanilla("minecraft:quartz", Profession.MINING)
        registerVanilla("minecraft:amethyst_shard", Profession.MINING)
    }

    private fun register(item: DeferredItem<*>, profession: Profession) {
        itemProfessionMap[item.id] = profession
    }

    private fun registerVanilla(item: String, profession: Profession) {
        itemProfessionMap[ResourceLocation.parse(item)] = profession
    }

    fun getProfessionForItem(stack: ItemStack): Profession? {
        val key = BuiltInRegistries.ITEM.getKey(stack.item)
        return itemProfessionMap[key]
    }

    fun getXpForQuality(quality: ItemQuality): Int = when (quality) {
        ItemQuality.COMMON -> 1
        ItemQuality.FINE -> 3
        ItemQuality.RARE -> 5
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
