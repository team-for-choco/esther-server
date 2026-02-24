package com.juyoung.estherserver.wild

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

object ModWild {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val RETURN_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<WildReturnData>> =
        ATTACHMENT_TYPES.register("wild_return_data", Supplier {
            AttachmentType.builder(Supplier { WildReturnData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, WildReturnData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): WildReturnData = WildReturnData.fromNBT(tag)

                    override fun write(
                        attachment: WildReturnData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
