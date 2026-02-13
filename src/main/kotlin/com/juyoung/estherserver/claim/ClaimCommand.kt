package com.juyoung.estherserver.claim

import com.juyoung.estherserver.EstherServerMod
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos

object ClaimCommand {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("claim")
                .then(
                    Commands.literal("info")
                        .executes { context -> info(context) }
                )
                .then(
                    Commands.literal("remove")
                        .executes { context -> remove(context) }
                )
                .then(
                    Commands.literal("list")
                        .executes { context -> list(context) }
                )
        )
    }

    private fun info(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())
        val claim = ChunkClaimManager.getClaimInfo(player.serverLevel(), chunkPos)

        if (claim == null) {
            player.displayClientMessage(
                Component.translatable(
                    "message.estherserver.claim_info_unclaimed",
                    chunkPos.x.toString(),
                    chunkPos.z.toString()
                ), false
            )
        } else {
            player.displayClientMessage(
                Component.translatable(
                    "message.estherserver.claim_info_claimed",
                    chunkPos.x.toString(),
                    chunkPos.z.toString(),
                    claim.ownerName
                ), false
            )
        }
        return 1
    }

    private fun remove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())

        val result = ChunkClaimManager.unclaim(player, chunkPos)

        when (result) {
            ChunkClaimManager.UnclaimResult.SUCCESS -> {
                val deedStack = ItemStack(EstherServerMod.LAND_DEED.get())
                if (!player.inventory.add(deedStack)) {
                    player.drop(deedStack, false)
                }
                player.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_removed",
                        chunkPos.x.toString(),
                        chunkPos.z.toString()
                    ), false
                )
            }
            ChunkClaimManager.UnclaimResult.NOT_CLAIMED -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.claim_not_claimed"), true
                )
            }
            ChunkClaimManager.UnclaimResult.NOT_OWNER -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.claim_not_owner"), true
                )
            }
        }
        return 1
    }

    private fun list(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val claims = ChunkClaimManager.getPlayerClaims(player.serverLevel(), player.uuid)

        if (claims.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.claim_list_empty"), false
            )
        } else {
            player.displayClientMessage(
                Component.translatable(
                    "message.estherserver.claim_list_header",
                    claims.size.toString()
                ), false
            )
            for ((chunkPos, _) in claims) {
                player.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_list_entry",
                        chunkPos.x.toString(),
                        chunkPos.z.toString()
                    ), false
                )
            }
        }
        return 1
    }
}
