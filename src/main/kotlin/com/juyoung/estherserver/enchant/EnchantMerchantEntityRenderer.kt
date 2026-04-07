package com.juyoung.estherserver.enchant

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import net.minecraft.client.resources.PlayerSkin
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class EnchantMerchantEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<EnchantMerchantEntity, PlayerRenderState, PlayerModel>(
        context,
        PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false),
        0.5f
    ) {

    companion object {
        private val SKIN = PlayerSkin(
            ResourceLocation.fromNamespaceAndPath("estherserver", "textures/entity/merchant/enchant_merchant.png"),
            null, null, null,
            PlayerSkin.Model.WIDE,
            true
        )
    }

    override fun getTextureLocation(state: PlayerRenderState): ResourceLocation = SKIN.texture()

    override fun createRenderState(): PlayerRenderState = PlayerRenderState()

    override fun scale(state: PlayerRenderState, poseStack: PoseStack) {
        poseStack.scale(0.9375f, 0.9375f, 0.9375f)
    }

    override fun extractRenderState(entity: EnchantMerchantEntity, state: PlayerRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver)
        state.skin = SKIN
        state.showHat = true
        state.showJacket = true
        state.showLeftPants = true
        state.showRightPants = true
        state.showLeftSleeve = true
        state.showRightSleeve = true
        state.showCape = false
    }
}
