package com.juyoung.estherserver.pet

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor

object PetHandler {

    fun handleRequestStorage(player: ServerPlayer) {
        val data = player.getData(ModPets.PET_DATA.get())
        val ownedNames = data.ownedPets.map { it.name }
        val summonedName = data.summonedPet?.name ?: ""
        PacketDistributor.sendToPlayer(player, PetStorageSyncPayload(ownedNames, summonedName))
    }

    fun handleSummonPet(player: ServerPlayer, petName: String) {
        val data = player.getData(ModPets.PET_DATA.get())
        val petType = PetType.fromName(petName) ?: return

        // Check ownership
        if (petType !in data.ownedPets) return

        // If already summoned same pet → dismiss
        if (data.summonedPet == petType) {
            dismissCurrentPet(player, data)
            return
        }

        // Dismiss any existing pet first
        if (data.summonedPet != null) {
            dismissCurrentPet(player, data)
        }

        // Spawn new pet
        val level = player.serverLevel()
        val pet = PetEntity(EstherServerMod.PET_ENTITY.get(), level)
        pet.petType = petType
        pet.moveTo(player.x, player.y, player.z, player.yRot, 0f)
        level.addFreshEntity(pet)
        player.startRiding(pet, true)

        data.summonedPet = petType
        data.summonedEntityId = pet.id
    }

    private fun dismissCurrentPet(player: ServerPlayer, data: PetData) {
        if (data.summonedEntityId != -1) {
            val entity = player.level().getEntity(data.summonedEntityId)
            if (entity is PetEntity) {
                player.stopRiding()
                entity.discard()
            }
        }
        data.summonedPet = null
        data.summonedEntityId = -1
    }
}
