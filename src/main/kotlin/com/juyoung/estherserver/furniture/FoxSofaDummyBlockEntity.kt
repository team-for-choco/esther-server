package com.juyoung.estherserver.furniture

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class FoxSofaDummyBlockEntity(
    pos: BlockPos,
    state: BlockState
) : AbstractSofaDummyBlockEntity(ModFurniture.FOX_SOFA_DUMMY_BE.get(), pos, state)
