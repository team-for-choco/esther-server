package com.juyoung.estherserver.claim

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.world.level.ChunkPos
import java.util.UUID

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
                .then(
                    Commands.literal("settings")
                        .executes { context -> settingsInfo(context) }
                        .then(
                            Commands.literal("break")
                                .then(Commands.literal("allow").executes { settingsUpdate(it, "break", true) })
                                .then(Commands.literal("deny").executes { settingsUpdate(it, "break", false) })
                        )
                        .then(
                            Commands.literal("place")
                                .then(Commands.literal("allow").executes { settingsUpdate(it, "place", true) })
                                .then(Commands.literal("deny").executes { settingsUpdate(it, "place", false) })
                        )
                        .then(
                            Commands.literal("interact")
                                .then(Commands.literal("allow").executes { settingsUpdate(it, "interact", true) })
                                .then(Commands.literal("deny").executes { settingsUpdate(it, "interact", false) })
                        )
                )
                .then(
                    Commands.literal("trust")
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("playerName", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            SharedSuggestionProvider.suggest(
                                                context.source.server.playerList.players
                                                    .filter { it.uuid != context.source.playerOrException.uuid }
                                                    .map { it.gameProfile.name },
                                                builder
                                            )
                                        }
                                        .executes { context -> trustAdd(context) }
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("playerName", StringArgumentType.word())
                                        .suggests { context, builder ->
                                            val player = context.source.playerOrException
                                            val trusted = ChunkClaimManager.getTrustedPlayers(player.serverLevel(), player.uuid)
                                            SharedSuggestionProvider.suggest(
                                                trusted.map { ChunkClaimManager.getTrustedPlayerName(player.serverLevel(), it) },
                                                builder
                                            )
                                        }
                                        .executes { context -> trustRemove(context) }
                                )
                        )
                        .then(
                            Commands.literal("list")
                                .executes { context -> trustList(context) }
                        )
                )
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("fakeclaim")
                                .executes { context -> fakeClaim(context) }
                        )
                        .then(
                            Commands.literal("forceremove")
                                .executes { context -> forceRemove(context) }
                        )
                )
        )
    }

    private fun permissionStatusText(allowed: Boolean): Component {
        return if (allowed)
            Component.translatable("message.estherserver.claim_perm_allow")
        else
            Component.translatable("message.estherserver.claim_perm_deny")
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
            val message = Component.translatable(
                "message.estherserver.claim_info_claimed",
                chunkPos.x.toString(),
                chunkPos.z.toString(),
                claim.ownerName
            )
            val perms = claim.permissions
            message.append(Component.literal("\n")).append(
                Component.translatable(
                    "message.estherserver.claim_info_perms",
                    permissionStatusText(perms.allowBreak),
                    permissionStatusText(perms.allowPlace),
                    permissionStatusText(perms.allowInteract)
                )
            )

            val data = ChunkClaimData.get(player.serverLevel())
            val trusted = data.getTrustedPlayers(claim.ownerUUID)
            if (trusted.isNotEmpty()) {
                if (claim.ownerUUID == player.uuid) {
                    val names = trusted.joinToString(", ") { data.getTrustedPlayerName(it) }
                    message.append(Component.literal("\n")).append(
                        Component.translatable("message.estherserver.claim_info_trusted", trusted.size.toString(), names)
                    )
                } else {
                    message.append(Component.literal("\n")).append(
                        Component.translatable("message.estherserver.claim_info_trusted_count", trusted.size.toString())
                    )
                }
            }

            player.displayClientMessage(message, false)
        }
        return 1
    }

    private fun settingsInfo(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())
        val claim = ChunkClaimManager.getClaimInfo(player.serverLevel(), chunkPos)

        if (claim == null) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.claim_not_claimed"), true
            )
            return 0
        }
        if (claim.ownerUUID != player.uuid) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.claim_not_owner"), true
            )
            return 0
        }

        val perms = claim.permissions
        val message = Component.translatable(
            "message.estherserver.claim_settings_header",
            chunkPos.x.toString(),
            chunkPos.z.toString()
        )
        message.append(Component.literal("\n")).append(
            Component.translatable("message.estherserver.claim_settings_break", permissionStatusText(perms.allowBreak))
        )
        message.append(Component.literal("\n")).append(
            Component.translatable("message.estherserver.claim_settings_place", permissionStatusText(perms.allowPlace))
        )
        message.append(Component.literal("\n")).append(
            Component.translatable("message.estherserver.claim_settings_interact", permissionStatusText(perms.allowInteract))
        )
        player.displayClientMessage(message, false)
        return 1
    }

    private fun settingsUpdate(context: CommandContext<CommandSourceStack>, type: String, allow: Boolean): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())

        val result = ChunkClaimManager.updatePermissions(player, chunkPos, type, allow)
        when (result) {
            ChunkClaimManager.UpdatePermResult.SUCCESS -> {
                val typeKey = "message.estherserver.claim_perm_type_$type"
                player.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_settings_updated",
                        Component.translatable(typeKey),
                        permissionStatusText(allow)
                    ), false
                )
            }
            ChunkClaimManager.UpdatePermResult.NOT_CLAIMED -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.claim_not_claimed"), true
                )
            }
            ChunkClaimManager.UpdatePermResult.NOT_OWNER -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.claim_not_owner"), true
                )
            }
        }
        return 1
    }

    private fun remove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())

        val result = ChunkClaimManager.unclaim(player, chunkPos)

        when (result) {
            ChunkClaimManager.UnclaimResult.SUCCESS -> {
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

    private fun fakeClaim(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())
        val data = ChunkClaimData.get(player.serverLevel())

        val existing = data.getClaim(chunkPos)
        if (existing != null) {
            player.displayClientMessage(
                Component.translatable(
                    "message.estherserver.claim_owned_by_other",
                    existing.ownerName
                ), true
            )
            return 0
        }

        data.setClaim(chunkPos, ChunkClaimEntry(
            ownerUUID = UUID.randomUUID(),
            ownerName = ClaimProtectionHandler.FAKE_PLAYER_NAME,
            claimedAt = player.serverLevel().gameTime
        ))
        player.displayClientMessage(
            Component.literal("[Admin] 청크 (${chunkPos.x}, ${chunkPos.z})를 FakePlayer 소유로 설정했습니다."),
            false
        )
        return 1
    }

    private fun forceRemove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val chunkPos = ChunkPos(player.blockPosition())
        val data = ChunkClaimData.get(player.serverLevel())

        val removed = data.removeClaim(chunkPos)
        if (removed != null) {
            player.displayClientMessage(
                Component.literal("[Admin] 청크 (${chunkPos.x}, ${chunkPos.z}) 클레임을 강제 해제했습니다. (소유자: ${removed.ownerName})"),
                false
            )
        } else {
            player.displayClientMessage(
                Component.translatable("message.estherserver.claim_not_claimed"), true
            )
        }
        return 1
    }

    private fun trustAdd(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val targetName = StringArgumentType.getString(context, "playerName")

        val targetProfile = player.server.profileCache?.get(targetName)?.orElse(null)
        if (targetProfile == null) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.trust_player_not_found", targetName), true
            )
            return 0
        }

        val result = ChunkClaimManager.addTrust(player, targetProfile.id, targetProfile.name)
        when (result) {
            ChunkClaimManager.TrustResult.SUCCESS -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_added", targetProfile.name), false
                )
                player.server.playerList.getPlayer(targetProfile.id)?.displayClientMessage(
                    Component.translatable("message.estherserver.trust_added_notify", player.gameProfile.name), false
                )
            }
            ChunkClaimManager.TrustResult.NO_CLAIMS -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_no_claims"), true
                )
            }
            ChunkClaimManager.TrustResult.ALREADY_TRUSTED -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_already", targetProfile.name), true
                )
            }
            ChunkClaimManager.TrustResult.CANNOT_TRUST_SELF -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_self"), true
                )
            }
        }
        return 1
    }

    private fun trustRemove(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val targetName = StringArgumentType.getString(context, "playerName")

        val data = ChunkClaimData.get(player.serverLevel())
        val trusted = data.getTrustedPlayers(player.uuid)
        val targetUUID = trusted.firstOrNull { data.getTrustedPlayerName(it) == targetName }

        if (targetUUID == null) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.trust_not_found", targetName), true
            )
            return 0
        }

        val result = ChunkClaimManager.removeTrust(player, targetUUID)
        when (result) {
            ChunkClaimManager.UntrustResult.SUCCESS -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_removed", targetName), false
                )
            }
            ChunkClaimManager.UntrustResult.NO_CLAIMS -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_no_claims"), true
                )
            }
            ChunkClaimManager.UntrustResult.NOT_TRUSTED -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.trust_not_found", targetName), true
                )
            }
        }
        return 1
    }

    private fun trustList(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val data = ChunkClaimData.get(player.serverLevel())
        val trusted = data.getTrustedPlayers(player.uuid)

        if (trusted.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.trust_list_empty"), false
            )
        } else {
            val message = Component.translatable(
                "message.estherserver.trust_list_header",
                trusted.size.toString()
            )
            for (trustedUUID in trusted) {
                val name = data.getTrustedPlayerName(trustedUUID)
                message.append(Component.literal("\n")).append(
                    Component.translatable("message.estherserver.trust_list_entry", name)
                )
            }
            player.displayClientMessage(message, false)
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
            val message = Component.translatable(
                "message.estherserver.claim_list_header",
                claims.size.toString()
            )
            for ((chunkPos, _) in claims) {
                message.append(Component.literal("\n")).append(
                    Component.translatable(
                        "message.estherserver.claim_list_entry",
                        chunkPos.x.toString(),
                        chunkPos.z.toString()
                    )
                )
            }
            player.displayClientMessage(message, false)
        }
        return 1
    }
}
