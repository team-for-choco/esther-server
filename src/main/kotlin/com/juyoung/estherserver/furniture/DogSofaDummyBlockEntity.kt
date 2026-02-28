package com.juyoung.estherserver.furniture

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class DogSofaDummyBlockEntity(
    pos: BlockPos,
    state: BlockState
) : AbstractSofaDummyBlockEntity(ModFurniture.DOG_SOFA_DUMMY_BE.get(), pos, state)
