package com.juyoung.estherserver.furniture

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class CatSofaDummyBlockEntity(
    pos: BlockPos,
    state: BlockState
) : AbstractSofaDummyBlockEntity(ModFurniture.CAT_SOFA_DUMMY_BE.get(), pos, state)
