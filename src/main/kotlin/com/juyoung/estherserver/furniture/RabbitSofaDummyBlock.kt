package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

class RabbitSofaDummyBlock(properties: Properties) : CatSofaDummyBlock(
    properties,
    masterBlockClass = RabbitSofaBlock::class.java,
    itemSupplier = Supplier { EstherServerMod.RABBIT_SOFA_ITEM.get() }
) {
    companion object {
        val CODEC: MapCodec<RabbitSofaDummyBlock> = simpleCodec(::RabbitSofaDummyBlock)
    }
    override fun codec(): MapCodec<out CatSofaDummyBlock> = Companion.CODEC
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = RabbitSofaDummyBlockEntity(pos, state)
}
