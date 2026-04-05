package com.juyoung.estherserver.claim

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
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
            Commands.literal("토지")
                .then(
                    Commands.literal("정보")
                        .executes { context -> info(context) }
                )
                .then(
                    Commands.literal("제거")
                        .executes { context -> remove(context) }
                )
                .then(
                    Commands.literal("목록")
                        .executes { context -> list(context) }
                )
                .then(
                    Commands.literal("세팅")
                        .executes { context -> settingsInfo(context) }
                        .then(
                            Commands.literal("파괴")
                                .then(Commands.literal("허용").executes { settingsUpdate(it, "break", true) })
                                .then(Commands.literal("금지").executes { settingsUpdate(it, "break", false) })
                        )
                        .then(
                            Commands.literal("설치")
                                .then(Commands.literal("허용").executes { settingsUpdate(it, "place", true) })
                                .then(Commands.literal("금지").executes { settingsUpdate(it, "place", false) })
                        )
                        .then(
                            Commands.literal("상호작용")
                                .then(Commands.literal("허용").executes { settingsUpdate(it, "interact", true) })
                                .then(Commands.literal("금지").executes { settingsUpdate(it, "interact", false) })
                        )
                )
                .then(
                    Commands.literal("플레이어")
                        .then(
                            Commands.literal("추가")
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
                            Commands.literal("제거")
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
                            Commands.literal("목록")
                                .executes { context -> trustList(context) }
                        )
                )
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(2) }
                        .then(
                            Commands.literal("fakeclaim")
                                .executes { context -> fakeClaim(context) }
                                .then(
                                    Commands.literal("range")
                                        .then(
                                            Commands.argument("반경", IntegerArgumentType.integer(1, 50))
                                                .executes { context -> fakeClaimRange(context) }
                                        )
                                )
                                .then(
                                    Commands.literal("area")
                                        .then(
                                            Commands.argument("cx1", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("cz1", IntegerArgumentType.integer())
                                                        .then(
                                                            Commands.argument("cx2", IntegerArgumentType.integer())
                                                                .then(
                                                                    Commands.argument("cz2", IntegerArgumentType.integer())
                                                                        .executes { context -> fakeClaimArea(context) }
                                                                )
                                                        )
                                                )
                                        )
                                )
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
        val claims = ChunkClaimManager.getPlayerClaims(player.serverLevel(), player.uuid)

        if (claims.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("message.estherserver.claim_list_empty"), true
            )
            return 0
        }

        val perms = claims.first().second.permissions
        val message = Component.translatable("message.estherserver.claim_settings_header_all", claims.size.toString())
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

        val (result, count) = ChunkClaimManager.updateAllPermissions(player, type, allow)
        when (result) {
            ChunkClaimManager.UpdateAllPermResult.SUCCESS -> {
                val typeKey = "message.estherserver.claim_perm_type_$type"
                player.displayClientMessage(
                    Component.translatable(
                        "message.estherserver.claim_settings_updated_all",
                        Component.translatable(typeKey),
                        permissionStatusText(allow),
                        count.toString()
                    ), false
                )
            }
            ChunkClaimManager.UpdateAllPermResult.NO_CLAIMS -> {
                player.displayClientMessage(
                    Component.translatable("message.estherserver.claim_list_empty"), true
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

    private fun fakeClaimRange(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val radius = IntegerArgumentType.getInteger(context, "반경")
        val center = ChunkPos(player.blockPosition())
        val data = ChunkClaimData.get(player.serverLevel())
        val gameTime = player.serverLevel().gameTime
        val fakeUUID = UUID.randomUUID()

        var claimed = 0
        var skipped = 0
        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                val chunkPos = ChunkPos(center.x + dx, center.z + dz)
                if (data.getClaim(chunkPos) != null) {
                    skipped++
                    continue
                }
                data.setClaim(chunkPos, ChunkClaimEntry(
                    ownerUUID = fakeUUID,
                    ownerName = ClaimProtectionHandler.FAKE_PLAYER_NAME,
                    claimedAt = gameTime
                ))
                claimed++
            }
        }
        player.displayClientMessage(
            Component.literal("[Admin] 반경 $radius 청크 범위 fakeclaim 완료: ${claimed}개 처리, ${skipped}개 이미 점유"),
            false
        )
        return 1
    }

    private fun fakeClaimArea(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val cx1 = IntegerArgumentType.getInteger(context, "cx1")
        val cz1 = IntegerArgumentType.getInteger(context, "cz1")
        val cx2 = IntegerArgumentType.getInteger(context, "cx2")
        val cz2 = IntegerArgumentType.getInteger(context, "cz2")

        val xMin = minOf(cx1, cx2)
        val xMax = maxOf(cx1, cx2)
        val zMin = minOf(cz1, cz2)
        val zMax = maxOf(cz1, cz2)
        val total = (xMax - xMin + 1) * (zMax - zMin + 1)

        if (total > 10_000) {
            player.displayClientMessage(
                Component.literal("[Admin] 범위가 너무 큽니다. (${total}개 > 최대 10,000개) 범위를 줄여주세요."),
                true
            )
            return 0
        }

        val data = ChunkClaimData.get(player.serverLevel())
        val gameTime = player.serverLevel().gameTime
        val fakeUUID = UUID.randomUUID()

        var claimed = 0
        var skipped = 0
        for (cx in xMin..xMax) {
            for (cz in zMin..zMax) {
                val chunkPos = ChunkPos(cx, cz)
                if (data.getClaim(chunkPos) != null) {
                    skipped++
                    continue
                }
                data.setClaim(chunkPos, ChunkClaimEntry(
                    ownerUUID = fakeUUID,
                    ownerName = ClaimProtectionHandler.FAKE_PLAYER_NAME,
                    claimedAt = gameTime
                ))
                claimed++
            }
        }
        player.displayClientMessage(
            Component.literal("[Admin] 청크 (${xMin},${zMin})~(${xMax},${zMax}) fakeclaim 완료: ${claimed}개 처리, ${skipped}개 이미 점유 (전체 ${total}개)"),
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
