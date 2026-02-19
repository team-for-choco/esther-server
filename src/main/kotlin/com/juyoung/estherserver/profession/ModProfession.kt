package com.juyoung.estherserver.profession

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.attachment.IAttachmentSerializer
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object ModProfession {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val PROFESSION_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<ProfessionData>> =
        ATTACHMENT_TYPES.register("profession_data", Supplier {
            AttachmentType.builder(Supplier { ProfessionData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, ProfessionData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): ProfessionData = ProfessionData.fromNBT(tag)

                    override fun write(
                        attachment: ProfessionData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
