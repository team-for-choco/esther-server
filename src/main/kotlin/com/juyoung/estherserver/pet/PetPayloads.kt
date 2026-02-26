package com.juyoung.estherserver.pet

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

/**
 * Client→Server: 펫 보관함 열기 요청.
 */
object RequestPetStoragePayload : CustomPacketPayload {
    override fun type() = TYPE

    val TYPE = CustomPacketPayload.Type<RequestPetStoragePayload>(
        ResourceLocation.fromNamespaceAndPath("estherserver", "request_pet_storage")
    )
    val STREAM_CODEC: StreamCodec<FriendlyByteBuf, RequestPetStoragePayload> =
        object : StreamCodec<FriendlyByteBuf, RequestPetStoragePayload> {
            override fun decode(buf: FriendlyByteBuf) = RequestPetStoragePayload
            override fun encode(buf: FriendlyByteBuf, value: RequestPetStoragePayload) {}
        }
}

/**
 * Server→Client: 보유 펫 목록 + 소환 상태 전송.
 */
class PetStorageSyncPayload(
    val ownedPets: List<String>,
    val summonedPet: String
) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<PetStorageSyncPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "pet_storage_sync")
        )
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, PetStorageSyncPayload> =
            object : StreamCodec<FriendlyByteBuf, PetStorageSyncPayload> {
                override fun decode(buf: FriendlyByteBuf): PetStorageSyncPayload {
                    val count = buf.readVarInt()
                    val pets = mutableListOf<String>()
                    repeat(count) { pets.add(buf.readUtf()) }
                    val summoned = buf.readUtf()
                    return PetStorageSyncPayload(pets, summoned)
                }
                override fun encode(buf: FriendlyByteBuf, value: PetStorageSyncPayload) {
                    buf.writeVarInt(value.ownedPets.size)
                    for (pet in value.ownedPets) { buf.writeUtf(pet) }
                    buf.writeUtf(value.summonedPet)
                }
            }
    }
}

/**
 * Client→Server: 펫 소환/해제 요청.
 */
class SummonPetPayload(val petName: String) : CustomPacketPayload {
    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<SummonPetPayload>(
            ResourceLocation.fromNamespaceAndPath("estherserver", "summon_pet")
        )
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, SummonPetPayload> =
            object : StreamCodec<FriendlyByteBuf, SummonPetPayload> {
                override fun decode(buf: FriendlyByteBuf) = SummonPetPayload(buf.readUtf())
                override fun encode(buf: FriendlyByteBuf, value: SummonPetPayload) {
                    buf.writeUtf(value.petName)
                }
            }
    }
}
