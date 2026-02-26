package com.juyoung.estherserver.pet

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.nbt.CompoundTag
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.attachment.IAttachmentSerializer
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier
import net.minecraft.core.HolderLookup

object ModPets {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val PET_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<PetData>> =
        ATTACHMENT_TYPES.register("pet_data", Supplier {
            AttachmentType.builder(Supplier { PetData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, PetData> {
                    override fun read(
                        holder: IAttachmentHolder, tag: CompoundTag, provider: HolderLookup.Provider
                    ): PetData = PetData.fromNBT(tag)

                    override fun write(
                        attachment: PetData, provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
