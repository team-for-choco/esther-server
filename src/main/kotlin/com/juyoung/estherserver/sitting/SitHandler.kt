package com.juyoung.estherserver.sitting

import com.juyoung.estherserver.EstherServerMod
import com.mojang.logging.LogUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.phys.AABB
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.level.BlockEvent

object SitHandler {

    private val LOGGER = LogUtils.getLogger()
    private const val LOWER_SEAT_Y_OFFSET = 0.3
    private const val UPPER_SEAT_Y_OFFSET = 0.8

    @SubscribeEvent
    fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
        if (event.hand != InteractionHand.MAIN_HAND) return

        val player = event.entity
        val level = event.level

        if (level.isClientSide) return
        if (!player.mainHandItem.isEmpty) return
        if (player.isShiftKeyDown) return
        if (player.isPassenger) return

        val pos = event.pos
        val state = level.getBlockState(pos)
        val block = state.block

        val seatY: Double = when {
            block is StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM -> {
                pos.y + LOWER_SEAT_Y_OFFSET
            }
            block is SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM -> {
                pos.y + LOWER_SEAT_Y_OFFSET
            }
            block is SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.TOP -> {
                pos.y + UPPER_SEAT_Y_OFFSET
            }
            else -> return
        }

        if (hasSeatAt(level, pos)) return

        val seat = SeatEntity(EstherServerMod.SEAT_ENTITY.get(), level)
        seat.setPos(pos.x + 0.5, seatY, pos.z + 0.5)

        if (!level.addFreshEntity(seat)) {
            LOGGER.warn("Failed to add SeatEntity at {}", pos)
            return
        }

        if (!player.startRiding(seat)) {
            LOGGER.warn("Failed to start riding SeatEntity at {}", pos)
            seat.discard()
            return
        }

        event.isCanceled = true
        LOGGER.debug("Player {} sat down at {}", player.name.string, pos)
    }

    @SubscribeEvent
    fun onBlockBreak(event: BlockEvent.BreakEvent) {
        val level = event.player.level()
        if (level.isClientSide) return
        removeSeatAt(level, event.pos)
    }

    private fun hasSeatAt(level: Level, pos: BlockPos): Boolean {
        return level.getEntitiesOfClass(SeatEntity::class.java, AABB(pos)).isNotEmpty()
    }

    private fun removeSeatAt(level: Level, pos: BlockPos) {
        level.getEntitiesOfClass(SeatEntity::class.java, AABB(pos)).forEach { it.discard() }
    }
}
