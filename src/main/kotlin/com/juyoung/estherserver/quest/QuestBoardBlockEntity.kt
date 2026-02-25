package com.juyoung.estherserver.quest

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class QuestBoardBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModQuest.QUEST_BOARD_BE.get(), pos, state)
