package com.juyoung.estherserver.quest

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

object ModQuest {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val QUEST_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<QuestData>> =
        ATTACHMENT_TYPES.register("quest_data", Supplier {
            AttachmentType.builder(Supplier { QuestData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, QuestData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): QuestData = QuestData.fromNBT(tag)

                    override fun write(
                        attachment: QuestData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })
}
