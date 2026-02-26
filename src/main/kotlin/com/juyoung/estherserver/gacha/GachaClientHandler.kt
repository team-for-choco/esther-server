package com.juyoung.estherserver.gacha

import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object GachaClientHandler {

    var cachedPayload: GachaRoulettePayload? = null
        internal set

    fun handleRoulettePayload(payload: GachaRoulettePayload) {
        cachedPayload = payload
    }
}
