package com.juyoung.estherserver.mixin;

import com.juyoung.estherserver.inventory.ProfessionInventoryHandler;
import com.juyoung.estherserver.inventory.ProfessionToolSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 특수 도구를 허용되지 않는 슬롯(상자 등)에 넣지 못하게 차단.
 * 허용 슬롯: 플레이어 인벤토리(Inventory) 또는 ProfessionToolSlot.
 */
@Mixin(Slot.class)
public class SpecialToolSlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void estherserver$blockSpecialToolPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;

        // 허용 슬롯이면 건너뜀
        if (self.container instanceof Inventory) return;
        if (self instanceof ProfessionToolSlot) return;
        // ProfessionSlot도 허용 (기존 전문 보관함 슬롯)
        if (self instanceof com.juyoung.estherserver.inventory.ProfessionSlot) return;

        if (ProfessionInventoryHandler.INSTANCE.isSpecialTool(stack)) {
            cir.setReturnValue(false);
        }
    }
}
