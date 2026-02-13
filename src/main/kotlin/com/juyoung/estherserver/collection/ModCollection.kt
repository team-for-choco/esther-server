package com.juyoung.estherserver.collection

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.attachment.IAttachmentSerializer
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

object ModCollection {
    val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EstherServerMod.MODID)

    val COLLECTION_DATA: DeferredHolder<AttachmentType<*>, AttachmentType<CollectionData>> =
        ATTACHMENT_TYPES.register("collection_data", Supplier {
            AttachmentType.builder(Supplier { CollectionData() })
                .serialize(object : IAttachmentSerializer<CompoundTag, CollectionData> {
                    override fun read(
                        holder: IAttachmentHolder,
                        tag: CompoundTag,
                        provider: HolderLookup.Provider
                    ): CollectionData = CollectionData.fromNBT(tag)

                    override fun write(
                        attachment: CollectionData,
                        provider: HolderLookup.Provider
                    ): CompoundTag = attachment.toNBT()
                })
                .copyOnDeath()
                .build()
        })

    val COLLECTIBLE_TAG: TagKey<Item> = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "collectible")
    )
}
