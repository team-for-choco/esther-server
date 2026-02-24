package com.juyoung.estherserver.mixin;

import com.juyoung.estherserver.EstherServerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    private static final TagKey<Block> OVERWORLD_BLOCKED_ORES = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(EstherServerMod.MODID, "overworld_blocked_ores")
    );

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void estherserver$blockOverworldOres(
            FeaturePlaceContext<OreConfiguration> context,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (context.level().getLevel().dimension() == Level.OVERWORLD) {
            for (OreConfiguration.TargetBlockState target : context.config().targetStates) {
                if (target.state.is(OVERWORLD_BLOCKED_ORES)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
