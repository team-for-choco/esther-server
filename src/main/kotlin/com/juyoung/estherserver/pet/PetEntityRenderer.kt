package com.juyoung.estherserver.pet

import com.juyoung.estherserver.EstherServerMod
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class PetEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<PetEntity, PetRenderState, PetEntityModel>(
        context,
        PetEntityModel(context.bakeLayer(PetEntityModel.LAYER_LOCATION)),
        0.4f
    ) {

    companion object {
        private val TEXTURE_MAP: Map<PetType, ResourceLocation> = PetType.entries.associateWith { type ->
            ResourceLocation.fromNamespaceAndPath(
                EstherServerMod.MODID,
                "textures/entity/pet/${type.textureId}.png"
            )
        }
        private val DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            EstherServerMod.MODID, "textures/entity/pet/cat_common.png"
        )
    }

    override fun getTextureLocation(state: PetRenderState): ResourceLocation {
        return TEXTURE_MAP[state.petType] ?: DEFAULT_TEXTURE
    }

    override fun createRenderState(): PetRenderState = PetRenderState()

    override fun extractRenderState(entity: PetEntity, state: PetRenderState, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        state.petType = entity.petType
    }

    override fun scale(state: PetRenderState, poseStack: PoseStack) {
        poseStack.scale(0.8f, 0.8f, 0.8f)
    }
}
