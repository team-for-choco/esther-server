package com.juyoung.estherserver.cooking

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class CookingStationBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModCooking.COOKING_STATION_BLOCK_ENTITY.get(), pos, state) {

    private val ingredients = mutableListOf<ItemStack>()

    fun addIngredient(stack: ItemStack) {
        ingredients.add(stack)
        setChanged()
    }

    fun getIngredients(): List<ItemStack> = ingredients.toList()

    fun getIngredientCount(): Int = ingredients.size

    fun clearIngredients() {
        ingredients.clear()
        setChanged()
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        val listTag = ListTag()
        for (stack in ingredients) {
            listTag.add(stack.save(registries))
        }
        tag.put("Ingredients", listTag)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        ingredients.clear()
        val listTag = tag.getList("Ingredients", 10) // 10 = CompoundTag
        for (i in 0 until listTag.size) {
            val itemTag = listTag.getCompound(i)
            val stack = ItemStack.parse(registries, itemTag)
            if (stack.isPresent) {
                ingredients.add(stack.get())
            }
        }
    }
}
