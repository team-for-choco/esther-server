package com.juyoung.estherserver.wild

import com.juyoung.estherserver.furniture.AbstractSofaDummyBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class PortalDummyBlockEntity(pos: BlockPos, state: BlockState) :
    AbstractSofaDummyBlockEntity(ModWild.PORTAL_DUMMY_BE.get(), pos, state)
