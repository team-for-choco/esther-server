package com.juyoung.estherserver.cooking

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.enhancement.EnhancementHandler
import com.juyoung.estherserver.profession.Profession
import com.juyoung.estherserver.profession.ProfessionBonusHelper
import com.juyoung.estherserver.profession.ProfessionHandler
import com.juyoung.estherserver.quality.ModDataComponents
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult

class CookingStationBlock(properties: Properties) : BaseEntityBlock(properties) {
    companion object {
        val CODEC: MapCodec<CookingStationBlock> = simpleCodec(::CookingStationBlock)
        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING
        private const val BASE_COOKING_TIME_SECONDS = 5.0f

        val COOKING_INGREDIENT_TAG: TagKey<Item> = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "cooking_ingredient")
        )
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return CookingStationBlockEntity(pos, state)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        return createTickerHelper(type, ModCooking.COOKING_STATION_BLOCK_ENTITY.get()) { lvl, pos, st, be ->
            CookingStationBlockEntity.serverTick(lvl, pos, st, be)
        }
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (stack.isEmpty) return InteractionResult.TRY_WITH_EMPTY_HAND
        if (level.isClientSide) return InteractionResult.SUCCESS

        // Cooking tool -> perform cooking
        if (stack.item === EstherServerMod.SPECIAL_COOKING_TOOL.get()) {
            return performCooking(level, pos, player, stack)
        }

        if (!stack.`is`(COOKING_INGREDIENT_TAG)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_not_ingredient"), true
            )
            return InteractionResult.FAIL
        }

        val blockEntity = level.getBlockEntity(pos) as? CookingStationBlockEntity
            ?: return InteractionResult.FAIL

        val playerUUID = player.uuid

        // Don't allow adding ingredients while cooking is in progress
        if (blockEntity.hasCookingTask(playerUUID)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_in_progress"), true
            )
            return InteractionResult.FAIL
        }

        // Check concurrent station limit on first ingredient
        if (blockEntity.getIngredientCount(playerUUID) == 0) {
            val serverPlayer = player as? ServerPlayer
            val equipLevel = if (serverPlayer != null) EnhancementHandler.getEquipmentLevel(serverPlayer, Profession.COOKING) else 0
            val maxStations = CookingStationTracker.getMaxConcurrentStations(equipLevel.coerceAtLeast(0))
            val activeCount = CookingStationTracker.getActiveCookingCount(playerUUID)
            if (activeCount >= maxStations) {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.cooking_max_stations", maxStations), true
                )
                return InteractionResult.FAIL
            }
            CookingStationTracker.addActiveCooking(playerUUID, pos)
        }

        val ingredientCopy = stack.copy()
        ingredientCopy.count = 1
        blockEntity.addIngredient(playerUUID, ingredientCopy)

        stack.shrink(1)

        val serverLevel = level as ServerLevel
        serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            pos.x + 0.5, pos.y + 1.1, pos.z + 0.5,
            8, 0.2, 0.1, 0.2, 0.01
        )
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f)

        val count = blockEntity.getIngredientCount(playerUUID)
        player.displayClientMessage(
            Component.translatable("message.estherserver.ingredient_added", count), true
        )

        return InteractionResult.SUCCESS
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS

        val blockEntity = level.getBlockEntity(pos) as? CookingStationBlockEntity
            ?: return InteractionResult.FAIL

        val playerUUID = player.uuid

        if (blockEntity.hasCookingTask(playerUUID)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_in_progress"), true
            )
            return InteractionResult.SUCCESS
        }

        if (blockEntity.getIngredientCount(playerUUID) > 0) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_need_tool"), true
            )
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    private fun performCooking(level: Level, pos: BlockPos, player: Player, toolStack: ItemStack): InteractionResult {
        val blockEntity = level.getBlockEntity(pos) as? CookingStationBlockEntity
            ?: return InteractionResult.FAIL

        val playerUUID = player.uuid
        val serverPlayer = player as? ServerPlayer

        // Already cooking at this station
        if (blockEntity.hasCookingTask(playerUUID)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_in_progress"), true
            )
            return InteractionResult.FAIL
        }

        if (blockEntity.getIngredientCount(playerUUID) == 0) {
            return InteractionResult.PASS
        }

        val serverLevel = level as ServerLevel
        val equipLevel = toolStack.getOrDefault(ModDataComponents.ENHANCEMENT_LEVEL.get(), 0)

        val recipeResult = CookingRecipeMatcher.findMatchingRecipe(level, blockEntity.getIngredients(playerUUID))

        if (recipeResult != null) {
            val resultStack = recipeResult.copy()

            // Lv4 cooking tool: 5% chance for double result
            if (equipLevel >= 4 && level.random.nextFloat() < 0.05f) {
                resultStack.count = 2
            }

            // Calculate cooking time (base 5s - profession reduction)
            val profLevel = if (serverPlayer != null) ProfessionHandler.getLevel(serverPlayer, Profession.COOKING) else 0
            val reduction = ProfessionBonusHelper.getCookingTimeReduction(profLevel)
            val cookingTimeSeconds = (BASE_COOKING_TIME_SECONDS - reduction).coerceAtLeast(1.0f)
            val cookingTicks = (cookingTimeSeconds * 20).toInt()

            // Start cooking task (deferred completion)
            blockEntity.startCookingTask(playerUUID, resultStack, cookingTicks)

            // Starting effects
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                pos.x + 0.5, pos.y + 1.1, pos.z + 0.5,
                10, 0.2, 0.1, 0.2, 0.01
            )
            level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8f, 1.0f)

            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_started", String.format("%.1f", cookingTimeSeconds)), true
            )
        } else {
            // Failure: ingredients lost immediately
            serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                20, 0.3, 0.3, 0.3, 0.02
            )
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 0.8f)

            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_failed"), true
            )
            CookingStationTracker.removeActiveCooking(playerUUID, pos)
        }

        blockEntity.clearIngredients(playerUUID)
        return InteractionResult.SUCCESS
    }
}
