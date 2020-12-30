package adudecalledleo.serversiding.menu;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MenuHandler {
    int getRowCount();

    void onOpen(ServerPlayerEntity player, Inventory inventory);
    /**
     * @return {@code true} to cancel click action, {@code false} otherwise
     */
    boolean onSlotClick(int slotId, int clickData, SlotActionType actionType, ServerPlayerEntity player, Inventory inventory);
    void onClose(ServerPlayerEntity player, Inventory inventory);

    default boolean canInsert(int slotId, ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
        return false;
    }

    static int slot(int col, int row) {
        return 9 * col + row;
    }
}
