package com.juyoung.estherserver.inventory

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

object ModInventory {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val PROFESSION_INVENTORY: DeferredHolder<AttachmentType<*>, AttachmentType<ProfessionInventoryData>> =
        ATTACHMENT_TYPES.register("profession_inventory", Supplier {
            AttachmentType.builder(Supplier { ProfessionInventoryData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, ProfessionInventoryData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): ProfessionInventoryData = ProfessionInventoryData.fromNBT(tag, provider)

                    override fun write(
                        attachment: ProfessionInventoryData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT(provider)
                })
                .copyOnDeath()
                .build()
        })
}
