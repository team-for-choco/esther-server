package com.juyoung.estherserver.pet

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
class PetEntityModel(root: ModelPart) : EntityModel<PetRenderState>(root) {

    private val head: ModelPart = root.getChild("head")
    private val legFL: ModelPart = root.getChild("leg_front_left")
    private val legFR: ModelPart = root.getChild("leg_front_right")
    private val legBL: ModelPart = root.getChild("leg_back_left")
    private val legBR: ModelPart = root.getChild("leg_back_right")
    private val tail: ModelPart = root.getChild("tail")

    override fun setupAnim(state: PetRenderState) {
        super.setupAnim(state)
        val ageInTicks = state.ageInTicks
        val walkSpeed = state.walkAnimationSpeed
        val walkPos = state.walkAnimationPos

        // Head follows yaw
        head.yRot = state.yRot * Mth.DEG_TO_RAD

        // Walking animation
        val swing = Mth.cos(walkPos * 0.6662f) * 1.2f * walkSpeed
        legFL.xRot = swing
        legBR.xRot = swing
        legFR.xRot = -swing
        legBL.xRot = -swing

        // Tail sway
        tail.yRot = Mth.cos(ageInTicks * 0.15f) * 0.3f
    }

    companion object {
        val LAYER_LOCATION = ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("estherserver", "pet"), "main"
        )

        fun createBodyLayer(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Body: 10x6x6, centered, y=14 (from ground)
            root.addOrReplaceChild("body",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-3.0f, -3.0f, -5.0f, 6.0f, 6.0f, 10.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f)
            )

            // Head: 6x6x5
            root.addOrReplaceChild("head",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-3.0f, -3.0f, -4.0f, 6.0f, 6.0f, 5.0f)
                    // Right ear
                    .texOffs(22, 16)
                    .addBox(-3.0f, -5.0f, -2.0f, 2.0f, 2.0f, 1.0f)
                    // Left ear
                    .texOffs(28, 16)
                    .addBox(1.0f, -5.0f, -2.0f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(0.0f, 15.0f, -5.0f)
            )

            // Front left leg: 2x4x2
            root.addOrReplaceChild("leg_front_left",
                CubeListBuilder.create()
                    .texOffs(0, 27)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offset(1.5f, 20.0f, -3.5f)
            )

            // Front right leg
            root.addOrReplaceChild("leg_front_right",
                CubeListBuilder.create()
                    .texOffs(8, 27)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offset(-1.5f, 20.0f, -3.5f)
            )

            // Back left leg
            root.addOrReplaceChild("leg_back_left",
                CubeListBuilder.create()
                    .texOffs(16, 27)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offset(1.5f, 20.0f, 3.5f)
            )

            // Back right leg
            root.addOrReplaceChild("leg_back_right",
                CubeListBuilder.create()
                    .texOffs(24, 27)
                    .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offset(-1.5f, 20.0f, 3.5f)
            )

            // Tail: 1x1x6
            root.addOrReplaceChild("tail",
                CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, 15.0f, 5.0f, -0.5f, 0.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }
}
