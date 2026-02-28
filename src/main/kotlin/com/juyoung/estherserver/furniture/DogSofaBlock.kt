package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import java.util.function.Supplier

class DogSofaBlock(properties: Properties) : CatSofaBlock(
    properties,
    dummyBlockSupplier = Supplier { EstherServerMod.DOG_SOFA_DUMMY.get() },
    noSpaceMessageKey = "message.estherserver.dog_sofa_no_space"
) {
    companion object {
        val CODEC: MapCodec<DogSofaBlock> = simpleCodec(::DogSofaBlock)
    }
    override fun codec(): MapCodec<out CatSofaBlock> = Companion.CODEC
}
