package adudecalledleo.serversiding.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class MenuScreenHandler extends ScreenHandler {
    private final MenuHandler menuHandler;
    private final SimpleInventory inventory;
    private final int rows;

    private static ScreenHandlerType<GenericContainerScreenHandler> rowCountToType(int rowCount) {
        switch (rowCount) {
        case 1:
            return ScreenHandlerType.GENERIC_9X1;
        case 2:
            return ScreenHandlerType.GENERIC_9X2;
        case 3:
            return ScreenHandlerType.GENERIC_9X3;
        case 4:
            return ScreenHandlerType.GENERIC_9X4;
        case 5:
            return ScreenHandlerType.GENERIC_9X5;
        case 6:
            return ScreenHandlerType.GENERIC_9X6;
        default:
            throw new RuntimeException("Size of 9x" + rowCount + " is not supported!");
        }
    }

    public MenuScreenHandler(int syncId, MenuHandler menuHandler, PlayerEntity player) {
        super(rowCountToType(menuHandler.getRowCount()), syncId);
        this.menuHandler = menuHandler;
        rows = menuHandler.getRowCount();

        inventory = new SimpleInventory(9 * rows);
        if (player instanceof ServerPlayerEntity)
            menuHandler.onOpen((ServerPlayerEntity) player, inventory);

        int i = (rows - 4) * 18;

        int n;
        int m;
        for(n = 0; n < rows; n++) {
            for(m = 0; m < 9; m++)
                addSlot(new Slot(inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
        }

        for(n = 0; n < 3; n++) {
            for(m = 0; m < 9; m++)
                addSlot(new Slot(player.inventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i));
        }

        for(n = 0; n < 9; n++)
            this.addSlot(new Slot(player.inventory, n, 8 + n * 18, 161 + i));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        // TODO actually implement this
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            if (slotId >= 0 && slotId < 9 * rows
                    && menuHandler.onSlotClick(slotId, clickData, actionType, (ServerPlayerEntity) player, inventory))
                return ItemStack.EMPTY;
        }
        return super.onSlotClick(slotId, clickData, actionType, player);
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        if (player instanceof ServerPlayerEntity)
            menuHandler.onClose((ServerPlayerEntity) player, inventory);
    }
}
