package com.juyoung.estherserver.enhancement

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

object ModEnhancement {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val PITY_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<EnhancementPityData>> =
        ATTACHMENT_TYPES.register("enhancement_pity_data", Supplier {
            AttachmentType.builder(Supplier { EnhancementPityData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, EnhancementPityData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): EnhancementPityData = EnhancementPityData.fromNBT(tag)

                    override fun write(
                        attachment: EnhancementPityData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
