package com.juyoung.estherserver.cosmetic

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

object ModCosmetics {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val COSMETIC_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<CosmeticData>> =
        ATTACHMENT_TYPES.register("cosmetic_data", Supplier {
            AttachmentType.builder(Supplier { CosmeticData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, CosmeticData> {
                    override fun read(
                        holder: IAttachmentHolder, tag: CompoundTag, provider: HolderLookup.Provider
                    ): CosmeticData = CosmeticData.fromNBT(tag)

                    override fun write(
                        attachment: CosmeticData, provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
