package com.juyoung.estherserver.economy

object EconomyClientHandler {
    var cachedBalance: Long = 0L
        private set

    fun handleSync(payload: BalanceSyncPayload) {
        cachedBalance = payload.balance
    }
}
