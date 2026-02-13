package com.juyoung.estherserver.cooking

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.UUID

class CookingStationBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModCooking.COOKING_STATION_BLOCK_ENTITY.get(), pos, state) {

    private val ingredientsByPlayer = mutableMapOf<UUID, MutableList<ItemStack>>()

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

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        val playersTag = CompoundTag()
        for ((uuid, stacks) in ingredientsByPlayer) {
            val listTag = ListTag()
            for (stack in stacks) {
                listTag.add(stack.save(registries))
            }
            playersTag.put(uuid.toString(), listTag)
        }
        tag.put("IngredientsByPlayer", playersTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        ingredientsByPlayer.clear()
        val playersTag = tag.getCompound("IngredientsByPlayer")
        for (key in playersTag.allKeys) {
            val uuid = try { UUID.fromString(key) } catch (_: Exception) { continue }
            val listTag = playersTag.getList(key, 10) // 10 = CompoundTag
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
    }
}
