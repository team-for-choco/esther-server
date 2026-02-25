package com.juyoung.estherserver.quest

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class QuestBoardDummyBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModQuest.QUEST_BOARD_DUMMY_BE.get(), pos, state) {

    var masterPos: BlockPos = BlockPos.ZERO
        private set

    fun setMasterPos(pos: BlockPos) {
        masterPos = pos
        setChanged()
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.putInt("MasterX", masterPos.x)
        tag.putInt("MasterY", masterPos.y)
        tag.putInt("MasterZ", masterPos.z)
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        masterPos = BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"))
    }
}
