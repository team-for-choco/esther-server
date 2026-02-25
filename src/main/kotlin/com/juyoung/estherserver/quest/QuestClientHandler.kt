package com.juyoung.estherserver.quest

object QuestClientHandler {
    var cachedData: QuestData = QuestData()
        private set

    fun handleSync(payload: QuestSyncPayload) {
        cachedData = payload.data
    }
}
