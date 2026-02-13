package com.juyoung.estherserver.collection

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent

object ChatTitleHandler {

    @SubscribeEvent
    fun onNameFormat(event: PlayerEvent.NameFormat) {
        val player = event.entity as? ServerPlayer ?: return
        val titlePrefix = getTitlePrefix(player) ?: return
        event.setDisplayname(Component.empty().append(titlePrefix).append(event.getDisplayname()))
    }

    @SubscribeEvent
    fun onTabListNameFormat(event: PlayerEvent.TabListNameFormat) {
        val player = event.entity as? ServerPlayer ?: return
        val titlePrefix = getTitlePrefix(player) ?: return
        val baseName: Component = event.getDisplayName() ?: player.displayName ?: player.name
        event.setDisplayName(Component.empty().append(titlePrefix).append(baseName))
    }

    private fun getTitlePrefix(player: ServerPlayer): Component? {
        val data = player.getData(ModCollection.COLLECTION_DATA.get())
        val activeTitleId = data.activeTitle ?: return null
        val milestone = Milestone.byId(activeTitleId) ?: return null
        return Component.literal("[")
            .append(Component.translatable(milestone.titleKey).withStyle(milestone.color))
            .append(Component.literal("] "))
    }
}
