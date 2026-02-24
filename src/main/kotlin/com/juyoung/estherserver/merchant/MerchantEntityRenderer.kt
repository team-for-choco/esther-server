package com.juyoung.estherserver.merchant

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
class MerchantEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<MerchantEntity, PlayerRenderState, PlayerModel>(
        context,
        PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false),
        0.5f
    ) {

    companion object {
        private val SKIN_MAP: Map<ShopCategory, PlayerSkin> = ShopCategory.entries.associateWith { cat ->
            PlayerSkin(
                ResourceLocation.fromNamespaceAndPath(
                    "estherserver",
                    "textures/entity/merchant/${cat.name.lowercase()}.png"
                ),
                null, null, null,
                PlayerSkin.Model.WIDE,
                true
            )
        }

        private val DEFAULT_SKIN = PlayerSkin(
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png"),
            null, null, null,
            PlayerSkin.Model.WIDE,
            true
        )
    }

    override fun getTextureLocation(state: PlayerRenderState): ResourceLocation {
        return state.skin.texture()
    }

    override fun createRenderState(): PlayerRenderState = PlayerRenderState()

    override fun scale(state: PlayerRenderState, poseStack: PoseStack) {
        poseStack.scale(0.9375f, 0.9375f, 0.9375f)
    }

    override fun extractRenderState(entity: MerchantEntity, state: PlayerRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver)

        state.skin = SKIN_MAP[entity.merchantType] ?: DEFAULT_SKIN

        // Show all model layers
        state.showHat = true
        state.showJacket = true
        state.showLeftPants = true
        state.showRightPants = true
        state.showLeftSleeve = true
        state.showRightSleeve = true
        state.showCape = false
    }
}
