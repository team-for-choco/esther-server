package com.juyoung.estherserver.pet

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class PetRenderState : LivingEntityRenderState() {
    var petType: PetType = PetType.CAT_COMMON
}
