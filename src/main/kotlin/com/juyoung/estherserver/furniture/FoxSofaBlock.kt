package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import java.util.function.Supplier

class FoxSofaBlock(properties: Properties) : CatSofaBlock(
    properties,
    dummyBlockSupplier = Supplier { EstherServerMod.FOX_SOFA_DUMMY.get() },
    noSpaceMessageKey = "message.estherserver.fox_sofa_no_space"
) {
    companion object {
        val CODEC: MapCodec<FoxSofaBlock> = simpleCodec(::FoxSofaBlock)
    }
    override fun codec(): MapCodec<out CatSofaBlock> = Companion.CODEC
}
