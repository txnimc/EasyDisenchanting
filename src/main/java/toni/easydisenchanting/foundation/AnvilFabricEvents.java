#if fabric
package toni.easydisenchanting.foundation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AnvilFabricEvents {

    @Getter
    public static class AnvilUpdateEvent {
        private final ItemStack left;
        private final ItemStack right;
        private final String name;
        private final Player player;

        @Setter
        private ItemStack output;
        @Setter
        private int cost;
        @Setter
        private int materialCost;

        public AnvilUpdateEvent(ItemStack left, ItemStack right, String name, int cost, Player player) {
            this.left = left;
            this.right = right;
            this.output = ItemStack.EMPTY;
            this.name = name;
            this.player = player;
            this.setCost(cost);
            this.setMaterialCost(0);
        }

    }

    @Getter
    public static class AnvilRepairEvent {
        private final @NotNull ItemStack left;
        private final @NotNull ItemStack right;
        private final @NotNull ItemStack output;
        private final Player entity;

        @Setter
        private float breakChance;

        public AnvilRepairEvent(Player player, @NotNull ItemStack left, @NotNull ItemStack right, @NotNull ItemStack output) {
            entity = player;
            this.output = output;
            this.left = left;
            this.right = right;
            this.setBreakChance(0.12F);
        }
    }

}
#endif