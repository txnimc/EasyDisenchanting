package toni.easydisenchanting.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import toni.easydisenchanting.AnvilModifier;

#if fabric
import toni.easydisenchanting.foundation.AnvilFabricEvents;
#endif

@Mixin({AnvilMenu.class})
public abstract class AnvilMenuMixin #if fabric extends ItemCombinerMenu #endif {
    #if fabric
    @Shadow
    @Final
    private DataSlot cost;

    @Shadow private String itemName;

    @Shadow public int repairItemCountCost;

    #if mc >= 214
    public AnvilMenuMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }
    #else
    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }
    #endif

    @Inject(
        method = "createResult()V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/inventory/AnvilMenu;repairItemCountCost:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void onAnvilUpdate(CallbackInfo cir, @Local(name = "itemStack") ItemStack itemstack, @Local(name = "itemStack3") ItemStack itemstack2, @Local(name = "j") int j) {
        AnvilFabricEvents.AnvilUpdateEvent event = new AnvilFabricEvents.AnvilUpdateEvent(itemstack, itemstack2, itemName, j, this.player);

        AnvilModifier.onUpdate(event);

        if (!event.getOutput().isEmpty()) {
            resultSlots.setItem(0, event.getOutput());
            cost.set(event.getCost());
            repairItemCountCost = event.getMaterialCost();
            cir.cancel();
        }
    }


    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void onAnvilRepair(Player player, ItemStack stack, CallbackInfo ci) {
        var event = new AnvilFabricEvents.AnvilRepairEvent(player, this.inputSlots.getItem(0), this.inputSlots.getItem(1), stack);
        AnvilModifier.onRepair(event);
    }
    #endif
}