package com.juyoung.estherserver.collection

object CollectionClientHandler {
    var cachedData: CollectionData = CollectionData()
        private set

    fun handleSync(payload: CollectionSyncPayload) {
        cachedData = payload.data
    }

    fun handleUpdate(payload: CollectionUpdatePayload) {
        cachedData.updateEntry(payload.key, payload.entry)
    }
}
