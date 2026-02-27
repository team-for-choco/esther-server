package com.juyoung.estherserver.mixin;

import com.juyoung.estherserver.cosmetic.CosmeticArmorItems;
import com.juyoung.estherserver.cosmetic.CosmeticClientHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * 치장 시스템 렌더링 Mixin.
 * HumanoidArmorLayer.render() 진입 시 플레이어의 치장 장착 상태를 확인하고,
 * 해당 슬롯의 equipment를 가상 ArmorItem으로 교체하여 치장 텍스처를 렌더링한다.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class CosmeticArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {

    @Unique
    private ItemStack estherserver$originalHead = ItemStack.EMPTY;
    @Unique
    private ItemStack estherserver$originalChest = ItemStack.EMPTY;
    @Unique
    private ItemStack estherserver$originalLegs = ItemStack.EMPTY;
    @Unique
    private ItemStack estherserver$originalFeet = ItemStack.EMPTY;
    @Unique
    private boolean estherserver$headSwapped = false;
    @Unique
    private boolean estherserver$chestSwapped = false;
    @Unique
    private boolean estherserver$legsSwapped = false;
    @Unique
    private boolean estherserver$feetSwapped = false;
    @Unique
    private boolean estherserver$didSwap = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void estherserver$beforeRender(
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            S renderState, float yRot, float xRot, CallbackInfo ci
    ) {
        estherserver$didSwap = false;
        estherserver$headSwapped = false;
        estherserver$chestSwapped = false;
        estherserver$legsSwapped = false;
        estherserver$feetSwapped = false;

        if (!(renderState instanceof PlayerRenderState playerState)) {
            return;
        }

        UUID playerUUID = estherserver$getPlayerUUID(playerState.id);
        if (playerUUID == null) {
            return;
        }

        // HEAD
        String headCosmetic = CosmeticClientHandler.INSTANCE.getCosmeticForPlayer(playerUUID, EquipmentSlot.HEAD);
        if (headCosmetic != null) {
            ItemStack virtualStack = CosmeticArmorItems.INSTANCE.getVirtualStack(headCosmetic);
            if (virtualStack != null) {
                estherserver$originalHead = renderState.headEquipment;
                renderState.headEquipment = virtualStack;
                estherserver$headSwapped = true;
            }
        }

        // CHEST
        String chestCosmetic = CosmeticClientHandler.INSTANCE.getCosmeticForPlayer(playerUUID, EquipmentSlot.CHEST);
        if (chestCosmetic != null) {
            ItemStack virtualStack = CosmeticArmorItems.INSTANCE.getVirtualStack(chestCosmetic);
            if (virtualStack != null) {
                estherserver$originalChest = renderState.chestEquipment;
                renderState.chestEquipment = virtualStack;
                estherserver$chestSwapped = true;
            }
        }

        // LEGS
        String legsCosmetic = CosmeticClientHandler.INSTANCE.getCosmeticForPlayer(playerUUID, EquipmentSlot.LEGS);
        if (legsCosmetic != null) {
            ItemStack virtualStack = CosmeticArmorItems.INSTANCE.getVirtualStack(legsCosmetic);
            if (virtualStack != null) {
                estherserver$originalLegs = renderState.legsEquipment;
                renderState.legsEquipment = virtualStack;
                estherserver$legsSwapped = true;
            }
        }

        // FEET
        String feetCosmetic = CosmeticClientHandler.INSTANCE.getCosmeticForPlayer(playerUUID, EquipmentSlot.FEET);
        if (feetCosmetic != null) {
            ItemStack virtualStack = CosmeticArmorItems.INSTANCE.getVirtualStack(feetCosmetic);
            if (virtualStack != null) {
                estherserver$originalFeet = renderState.feetEquipment;
                renderState.feetEquipment = virtualStack;
                estherserver$feetSwapped = true;
            }
        }

        estherserver$didSwap = estherserver$headSwapped || estherserver$chestSwapped
                || estherserver$legsSwapped || estherserver$feetSwapped;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void estherserver$afterRender(
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            S renderState, float yRot, float xRot, CallbackInfo ci
    ) {
        if (!estherserver$didSwap || !(renderState instanceof PlayerRenderState)) {
            return;
        }

        // swap한 슬롯만 복원
        if (estherserver$headSwapped) {
            renderState.headEquipment = estherserver$originalHead;
            estherserver$originalHead = ItemStack.EMPTY;
        }
        if (estherserver$chestSwapped) {
            renderState.chestEquipment = estherserver$originalChest;
            estherserver$originalChest = ItemStack.EMPTY;
        }
        if (estherserver$legsSwapped) {
            renderState.legsEquipment = estherserver$originalLegs;
            estherserver$originalLegs = ItemStack.EMPTY;
        }
        if (estherserver$feetSwapped) {
            renderState.feetEquipment = estherserver$originalFeet;
            estherserver$originalFeet = ItemStack.EMPTY;
        }

        estherserver$didSwap = false;
    }

    @Unique
    private UUID estherserver$getPlayerUUID(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof Player player) {
            return player.getUUID();
        }
        return null;
    }
}
