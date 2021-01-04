package adudecalledleo.serversiding.menu.simple;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SlotListener {
    void onChanged(int slotId, @NotNull ServerPlayerEntity player, @NotNull Inventory inventory, @NotNull MenuState menuState);

    default boolean canInsert(int slotId, @NotNull ItemStack stack,
            @NotNull ServerPlayerEntity player, @NotNull Inventory inventory) {
        return true;
    }

    default boolean canExtract(int slotId, @NotNull ServerPlayerEntity player, @NotNull Inventory inventory) {
        return true;
    }

    default int getMaxItemCount(int slotId, @NotNull ServerPlayerEntity player, @NotNull Inventory inventory) {
        return 64;
    }

    default int getMaxItemCount(int slotId, @NotNull ItemStack stack,
            @NotNull ServerPlayerEntity player, @NotNull Inventory inventory) {
        return getMaxItemCount(slotId, player, inventory);
    }

    default boolean doDropOnClose(int slotId, @NotNull ServerPlayerEntity player, @NotNull Inventory inventory) {
        return true;
    }
}
