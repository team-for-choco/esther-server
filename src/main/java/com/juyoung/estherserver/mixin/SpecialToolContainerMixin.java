package com.juyoung.estherserver.mixin;

import com.juyoung.estherserver.inventory.ProfessionInventoryHandler;
import com.juyoung.estherserver.inventory.ProfessionInventoryMenu;
import com.juyoung.estherserver.inventory.ProfessionToolSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 특수 도구의 번호키(SWAP) 교환 + PICKUP 스왑을 차단.
 * mayPlace만으로는 커서에 들고 클릭하는 PICKUP 교환과
 * 번호키(SWAP) 동작을 완전히 막을 수 없기 때문에 doClick에서 직접 차단.
 */
@Mixin(AbstractContainerMenu.class)
public class SpecialToolContainerMixin {

    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void estherserver$blockSpecialToolSwap(
            int slotIndex, int button, ClickType clickType, net.minecraft.world.entity.player.Player player,
            CallbackInfo ci
    ) {
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;

        // 전문 보관함 메뉴에서는 자체 로직으로 처리하므로 건너뜀
        if (self instanceof ProfessionInventoryMenu) return;

        if (slotIndex < 0 || slotIndex >= self.slots.size()) return;
        Slot targetSlot = self.slots.get(slotIndex);

        if (clickType == ClickType.SWAP) {
            // 번호키로 핫바 아이템을 비허용 슬롯과 교환 시도
            ItemStack hotbarStack = player.getInventory().getItem(button);
            if (ProfessionInventoryHandler.INSTANCE.isSpecialTool(hotbarStack) && !isAllowedSlot(targetSlot)) {
                ci.cancel();
                return;
            }
            // 반대 방향: 슬롯의 특수 도구를 핫바로 가져오는 것은 OK (플레이어 인벤토리이므로)
        }

        if (clickType == ClickType.PICKUP) {
            // 커서에 특수 도구를 들고 비허용 슬롯에 놓기 시도
            ItemStack carried = self.getCarried();
            if (ProfessionInventoryHandler.INSTANCE.isSpecialTool(carried) && !isAllowedSlot(targetSlot)) {
                ci.cancel();
                return;
            }
        }
    }

    private static boolean isAllowedSlot(Slot slot) {
        if (slot.container instanceof Inventory) return true;
        if (slot instanceof ProfessionToolSlot) return true;
        if (slot instanceof com.juyoung.estherserver.inventory.ProfessionSlot) return true;
        return false;
    }
}
