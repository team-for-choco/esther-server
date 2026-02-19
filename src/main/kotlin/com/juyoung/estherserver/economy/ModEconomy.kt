package com.juyoung.estherserver.economy

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

object ModEconomy {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val BALANCE_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<BalanceData>> =
        ATTACHMENT_TYPES.register("balance_data", Supplier {
            AttachmentType.builder(Supplier { BalanceData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, BalanceData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): BalanceData = BalanceData.fromNBT(tag)

                    override fun write(
                        attachment: BalanceData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
