package com.juyoung.estherserver.profession

object ProfessionClientHandler {
    var cachedData: ProfessionData = ProfessionData()
        private set

    fun handleSync(payload: ProfessionSyncPayload) {
        cachedData = payload.data
    }
}
