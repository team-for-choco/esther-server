package com.juyoung.estherserver.cooking

import com.juyoung.estherserver.EstherServerMod
import com.juyoung.estherserver.profession.Profession
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
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult

class CookingStationBlock(properties: Properties) : BaseEntityBlock(properties) {
    companion object {
        val CODEC: MapCodec<CookingStationBlock> = simpleCodec(::CookingStationBlock)
        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING
        private const val MAX_INGREDIENTS = 4

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

        // 요리 재료 태그 검증
        if (!stack.`is`(COOKING_INGREDIENT_TAG)) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_not_ingredient"), true
            )
            return InteractionResult.FAIL
        }

        val blockEntity = level.getBlockEntity(pos) as? CookingStationBlockEntity
            ?: return InteractionResult.FAIL

        val playerUUID = player.uuid

        if (blockEntity.getIngredientCount(playerUUID) >= MAX_INGREDIENTS) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_station_full"), true
            )
            return InteractionResult.FAIL
        }

        // Store the ingredient (with quality data)
        val ingredientCopy = stack.copy()
        ingredientCopy.count = 1
        blockEntity.addIngredient(playerUUID, ingredientCopy)

        // Consume one item from player
        stack.shrink(1)

        // Feedback: particle + sound
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

        if (blockEntity.getIngredientCount(playerUUID) == 0) {
            return InteractionResult.PASS
        }

        val serverLevel = level as ServerLevel
        val recipeResult = CookingRecipeMatcher.findMatchingRecipe(level, blockEntity.getIngredients(playerUUID))

        if (recipeResult != null) {
            // Success: determine quality and spawn result
            val quality = CookingQualityCalculator.calculateQuality(
                blockEntity.getIngredients(playerUUID), level.random
            )
            val resultStack = recipeResult.copy()
            resultStack.set(ModDataComponents.ITEM_QUALITY.get(), quality)

            // Spawn the item
            val itemEntity = ItemEntity(
                level,
                pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                resultStack
            )
            itemEntity.setDefaultPickUpDelay()
            level.addFreshEntity(itemEntity)

            // Success effects
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.x + 0.5, pos.y + 1.2, pos.z + 0.5,
                15, 0.3, 0.2, 0.3, 0.05
            )
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.7f, 1.5f)

            // Grant cooking profession XP
            val serverPlayer = player as? ServerPlayer
            if (serverPlayer != null) {
                val xp = ProfessionHandler.getXpForQuality(quality)
                ProfessionHandler.addExperience(serverPlayer, Profession.COOKING, xp)
            }

            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_success"), true
            )
        } else {
            // Failure: ingredients lost
            serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                20, 0.3, 0.3, 0.3, 0.02
            )
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 0.8f)

            player.displayClientMessage(
                Component.translatable("message.estherserver.cooking_failed"), true
            )
        }

        blockEntity.clearIngredients(playerUUID)
        return InteractionResult.SUCCESS
    }
}
