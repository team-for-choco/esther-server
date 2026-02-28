package com.juyoung.estherserver.furniture

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class RabbitSofaDummyBlockEntity(
    pos: BlockPos,
    state: BlockState
) : AbstractSofaDummyBlockEntity(ModFurniture.RABBIT_SOFA_DUMMY_BE.get(), pos, state)
