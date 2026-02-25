package com.juyoung.estherserver.cooking

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.UUID

class CookingStationBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModCooking.COOKING_STATION_BLOCK_ENTITY.get(), pos, state) {

    private val ingredientsByPlayer = mutableMapOf<UUID, MutableList<ItemStack>>()

    data class CookingTask(
        val playerUUID: UUID,
        val resultStack: ItemStack,
        var remainingTicks: Int
    )

    val cookingTasks = mutableMapOf<UUID, CookingTask>()

    fun addIngredient(playerUUID: UUID, stack: ItemStack) {
        ingredientsByPlayer.getOrPut(playerUUID) { mutableListOf() }.add(stack)
        setChanged()
    }

    fun getIngredients(playerUUID: UUID): List<ItemStack> =
        ingredientsByPlayer[playerUUID]?.toList() ?: emptyList()

    fun getIngredientCount(playerUUID: UUID): Int =
        ingredientsByPlayer[playerUUID]?.size ?: 0

    fun clearIngredients(playerUUID: UUID) {
        ingredientsByPlayer.remove(playerUUID)
        setChanged()
    }

    fun startCookingTask(playerUUID: UUID, resultStack: ItemStack, cookingTicks: Int) {
        cookingTasks[playerUUID] = CookingTask(playerUUID, resultStack, cookingTicks)
        setChanged()
    }

    fun hasCookingTask(playerUUID: UUID): Boolean = cookingTasks.containsKey(playerUUID)

    companion object {
        fun serverTick(level: Level, pos: BlockPos, state: BlockState, blockEntity: CookingStationBlockEntity) {
            if (level.isClientSide) return
            val serverLevel = level as ServerLevel

            val completedTasks = mutableListOf<UUID>()

            for ((uuid, task) in blockEntity.cookingTasks) {
                task.remainingTicks--

                // Periodic cooking particles + sound (every 20 ticks)
                if (task.remainingTicks > 0 && task.remainingTicks % 20 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.x + 0.5, pos.y + 1.1, pos.z + 0.5,
                        5, 0.2, 0.1, 0.2, 0.01
                    )
                    level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.5f, 1.0f)
                }

                if (task.remainingTicks <= 0) {
                    // Spawn result item
                    val itemEntity = ItemEntity(
                        level,
                        pos.x + 0.5, pos.y + 1.0, pos.z + 0.5,
                        task.resultStack
                    )
                    itemEntity.setDefaultPickUpDelay()
                    level.addFreshEntity(itemEntity)

                    // Grant cooking profession XP on completion (fixed 1 XP)
                    val player = serverLevel.server.playerList.getPlayer(uuid)
                    if (player is ServerPlayer) {
                        com.juyoung.estherserver.profession.ProfessionHandler.addExperience(
                            player, com.juyoung.estherserver.profession.Profession.COOKING, 1
                        )
                    }

                    // Completion effects
                    serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        pos.x + 0.5, pos.y + 1.2, pos.z + 0.5,
                        15, 0.3, 0.2, 0.3, 0.05
                    )
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.7f, 1.5f)

                    completedTasks.add(uuid)
                }
            }

            if (completedTasks.isNotEmpty()) {
                for (uuid in completedTasks) {
                    blockEntity.cookingTasks.remove(uuid)
                    CookingStationTracker.removeActiveCooking(uuid, pos)
                }
                blockEntity.setChanged()
            }
        }
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        // Save ingredients
        val playersTag = CompoundTag()
        for ((uuid, stacks) in ingredientsByPlayer) {
            val listTag = ListTag()
            for (stack in stacks) {
                listTag.add(stack.save(registries))
            }
            playersTag.put(uuid.toString(), listTag)
        }
        tag.put("IngredientsByPlayer", playersTag)

        // Save cooking tasks
        val tasksTag = CompoundTag()
        for ((uuid, task) in cookingTasks) {
            val taskTag = CompoundTag()
            taskTag.put("Result", task.resultStack.save(registries))
            taskTag.putInt("RemainingTicks", task.remainingTicks)
            tasksTag.put(uuid.toString(), taskTag)
        }
        tag.put("CookingTasks", tasksTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        // Load ingredients
        ingredientsByPlayer.clear()
        val playersTag = tag.getCompound("IngredientsByPlayer")
        for (key in playersTag.allKeys) {
            val uuid = try { UUID.fromString(key) } catch (_: Exception) { continue }
            val listTag = playersTag.getList(key, 10)
            val stacks = mutableListOf<ItemStack>()
            for (i in 0 until listTag.size) {
                val itemTag = listTag.getCompound(i)
                val stack = ItemStack.parse(registries, itemTag)
                if (stack.isPresent) {
                    stacks.add(stack.get())
                }
            }
            if (stacks.isNotEmpty()) {
                ingredientsByPlayer[uuid] = stacks
            }
        }

        // Load cooking tasks
        cookingTasks.clear()
        if (tag.contains("CookingTasks")) {
            val tasksTag = tag.getCompound("CookingTasks")
            for (key in tasksTag.allKeys) {
                val uuid = try { UUID.fromString(key) } catch (_: Exception) { continue }
                val taskTag = tasksTag.getCompound(key)
                val resultStack = ItemStack.parse(registries, taskTag.getCompound("Result"))
                val remainingTicks = taskTag.getInt("RemainingTicks")
                if (resultStack.isPresent && remainingTicks > 0) {
                    cookingTasks[uuid] = CookingTask(uuid, resultStack.get(), remainingTicks)
                }
            }
        }
    }
}
