package com.juyoung.estherserver.enhancement

import com.juyoung.estherserver.profession.Profession

object EnhancementClientHandler {
    var cachedPityData: EnhancementPityData = EnhancementPityData()
        private set

    fun handlePitySync(payload: EnhancementPitySyncPayload) {
        cachedPityData = payload.data
    }

    fun getPity(profession: Profession): Double = cachedPityData.getPity(profession)

    fun getPityPercent(profession: Profession): Int = cachedPityData.getPityPercent(profession)
}
