package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

class DogSofaDummyBlock(properties: Properties) : CatSofaDummyBlock(
    properties,
    masterBlockClass = DogSofaBlock::class.java,
    itemSupplier = Supplier { EstherServerMod.DOG_SOFA_ITEM.get() }
) {
    companion object {
        val CODEC: MapCodec<DogSofaDummyBlock> = simpleCodec(::DogSofaDummyBlock)
    }
    override fun codec(): MapCodec<out CatSofaDummyBlock> = Companion.CODEC
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = DogSofaDummyBlockEntity(pos, state)
}
