package com.juyoung.estherserver.pet

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.network.PacketDistributor

object PetHandler {

    /**
     * 로그인 시 소환 상태가 남아있으면 펫을 자동 재소환.
     * 엔티티는 저장되지 않으므로 새로 생성해서 탑승시킨다.
     */
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val data = player.getData(ModPets.PET_DATA.get())
        val petType = data.summonedPet ?: return

        val level = player.serverLevel()
        val pet = PetEntity(EstherServerMod.PET_ENTITY.get(), level)
        pet.petType = petType
        pet.moveTo(player.x, player.y, player.z, player.yRot, 0f)
        level.addFreshEntity(pet)
        player.startRiding(pet, true)

        data.summonedEntityId = pet.id
    }

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

        // Close GUI on client by sending updated sync
        handleRequestStorage(player)
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
