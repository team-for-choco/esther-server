package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModFurniture {
    val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EstherServerMod.MODID)

    val CAT_SOFA_DUMMY_BE: DeferredHolder<BlockEntityType<*>, BlockEntityType<CatSofaDummyBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("cat_sofa_dummy", Supplier {
            BlockEntityType(::CatSofaDummyBlockEntity, EstherServerMod.CAT_SOFA_DUMMY.get())
        })

    val DOG_SOFA_DUMMY_BE: DeferredHolder<BlockEntityType<*>, BlockEntityType<DogSofaDummyBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("dog_sofa_dummy", Supplier {
            BlockEntityType(::DogSofaDummyBlockEntity, EstherServerMod.DOG_SOFA_DUMMY.get())
        })

    val RABBIT_SOFA_DUMMY_BE: DeferredHolder<BlockEntityType<*>, BlockEntityType<RabbitSofaDummyBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("rabbit_sofa_dummy", Supplier {
            BlockEntityType(::RabbitSofaDummyBlockEntity, EstherServerMod.RABBIT_SOFA_DUMMY.get())
        })

    val FOX_SOFA_DUMMY_BE: DeferredHolder<BlockEntityType<*>, BlockEntityType<FoxSofaDummyBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("fox_sofa_dummy", Supplier {
            BlockEntityType(::FoxSofaDummyBlockEntity, EstherServerMod.FOX_SOFA_DUMMY.get())
        })
}
