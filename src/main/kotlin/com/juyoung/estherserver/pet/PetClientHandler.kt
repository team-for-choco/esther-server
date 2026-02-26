package com.juyoung.estherserver.pet

object PetClientHandler {
    var cachedOwnedPets: List<PetType> = emptyList()
        internal set
    var cachedSummonedPet: PetType? = null
        internal set

    fun handleSync(payload: PetStorageSyncPayload) {
        cachedOwnedPets = payload.ownedPets.mapNotNull { PetType.fromName(it) }
        cachedSummonedPet = if (payload.summonedPet.isNotEmpty()) {
            PetType.fromName(payload.summonedPet)
        } else null
    }
}
