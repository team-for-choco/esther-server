package com.juyoung.estherserver.quest

object QuestClientHandler {
    var cachedData: QuestData = QuestData()
        internal set

    fun handleSync(payload: QuestSyncPayload) {
        cachedData = payload.data
    }
}
