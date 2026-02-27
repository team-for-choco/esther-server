package com.juyoung.estherserver.furniture

import com.juyoung.estherserver.EstherServerMod
import com.mojang.serialization.MapCodec
import java.util.function.Supplier

class RabbitSofaBlock(properties: Properties) : CatSofaBlock(
    properties,
    dummyBlockSupplier = Supplier { EstherServerMod.RABBIT_SOFA_DUMMY.get() },
    noSpaceMessageKey = "message.estherserver.rabbit_sofa_no_space"
) {
    companion object {
        val CODEC: MapCodec<RabbitSofaBlock> = simpleCodec(::RabbitSofaBlock)
    }
    override fun codec(): MapCodec<out CatSofaBlock> = Companion.CODEC
}
