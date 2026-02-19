package com.juyoung.estherserver.merchant

import net.minecraft.client.model.VillagerModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.VillagerRenderState
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class MerchantEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<MerchantEntity, VillagerRenderState, VillagerModel>(
        context,
        VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)),
        0.5f
    ) {

    companion object {
        private val TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png")
    }

    override fun getTextureLocation(renderState: VillagerRenderState): ResourceLocation = TEXTURE

    override fun createRenderState(): VillagerRenderState = VillagerRenderState()

    override fun extractRenderState(entity: MerchantEntity, state: VillagerRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        state.isUnhappy = false
    }
}
