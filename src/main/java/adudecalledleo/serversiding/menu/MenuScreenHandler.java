package adudecalledleo.serversiding.menu;

import adudecalledleo.serversiding.impl.mixin.ScreenHandlerAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

class MenuScreenHandler extends ScreenHandler {
    private final MenuHandler menuHandler;
    private final SimpleInventory inventory;
    private final int rows;

    private static ScreenHandlerType<GenericContainerScreenHandler> rowCountToType(int rowCount) {
        return switch (rowCount) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new RuntimeException("Size of 9x" + rowCount + " is not supported!");
        };
    }

    private final class MenuSlot extends Slot {
        private final int slotId;
        private final ServerPlayerEntity player;

        public MenuSlot(int index, int x, int y, ServerPlayerEntity player) {
            super(MenuScreenHandler.this.inventory, index, x, y);
            slotId = index;
            this.player = player;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return menuHandler.canInsert(slotId, stack, player, inventory);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return menuHandler.canExtract(slotId, player, inventory);
        }

        @Override
        public int getMaxItemCount() {
            return menuHandler.getMaxItemCount(slotId, player, inventory);
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return menuHandler.getMaxItemCount(slotId, stack, player, inventory);
        }
    }

    public MenuScreenHandler(int syncId, MenuHandler menuHandler, PlayerEntity player) {
        super(rowCountToType(menuHandler.getRowCount()), syncId);
        this.menuHandler = menuHandler;
        rows = menuHandler.getRowCount();

        inventory = new SimpleInventory(9 * rows);
        if (player instanceof ServerPlayerEntity)
            menuHandler.onOpen((ServerPlayerEntity) player, inventory);

        int invHeight = (rows - 4) * 18;

        int row;
        int col;
        if (player instanceof ServerPlayerEntity) {
            for (row = 0; row < rows; row++) {
                for (col = 0; col < 9; col++)
                    addSlot(new MenuSlot(col + row * 9, 8 + col * 18, 18 + row * 18, (ServerPlayerEntity) player));
            }
        } else {
            for (row = 0; row < rows; row++) {
                for (col = 0; col < 9; col++)
                    addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (row = 0; row < 3; row++) {
            for (col = 0; col < 9; col++)
                addSlot(new Slot(player.getInventory(), col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + invHeight));
        }

        for (row = 0; row < 9; row++)
            this.addSlot(new Slot(player.getInventory(), row, 8 + row * 18, 161 + invHeight));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private boolean blockContentUpdates;

    @Override
    public void sendContentUpdates() {
        if (!blockContentUpdates)
            super.sendContentUpdates();
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < 9 * rows) {
                if (!insertItem(itemStack2, 9 * rows, slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!insertItem(itemStack2, 0, 9 * rows, false))
                return ItemStack.EMPTY;

            if (itemStack2.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
        }

        return itemStack;
    }

    private void sendInventoryPacket(ScreenHandler handler, ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new InventoryS2CPacket(handler.syncId, handler.nextRevision(),
                handler.getStacks(), handler.getCursorStack()));
    }

    private void sendSlotUpdatePacket(ServerPlayerEntity player, int slotId) {
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(),
                slotId, slots.get(slotId).getStack()));
    }

    private void sendCursorStackUpdatePacket(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, nextRevision(), -1, getCursorStack()));
    }

    @Override
    public void onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity player) {
        boolean cancel = false;
        if (player instanceof ServerPlayerEntity) {
            if (slotId >= 0 && slotId < 9 * rows)
                cancel = menuHandler.onSlotClick(slotId, clickData, actionType, (ServerPlayerEntity) player, inventory);
        }
        if (cancel) {
            if (actionType == SlotActionType.SWAP) {
                // need to resync source and destination slots
                sendSlotUpdatePacket((ServerPlayerEntity) player, slotId);
                sendSlotUpdatePacket((ServerPlayerEntity) player, 9 * rows + 27 + clickData);
                //                                              our inv + player's main inv + hotbar index
                return;
            }
            if (actionType == SlotActionType.CLONE) {
                // need to resync cursor stack
                sendCursorStackUpdatePacket((ServerPlayerEntity) player);
                return;
            }
            if (actionType == SlotActionType.QUICK_CRAFT && ScreenHandler.unpackQuickCraftStage(clickData) < 2)
                // simple cancel (only do advanced cancel for QUICK_CRAFT after it's "finished")
                return;
            if (actionType == SlotActionType.THROW || actionType == SlotActionType.PICKUP)
                // simple cancel (required for THROW, a nice optimization for PICKUP)
                return;
        }
        DefaultedList<ItemStack> savedStacks = null;
        ItemStack savedCursorStack = ItemStack.EMPTY;
        if (cancel) {
            // QUICK_CRAFT, QUICK_MOVE or PICKUP_ALL - actions which change multiple slots
            // there are more efficient ways to do this, but for now let's just save and restore all slots (and cursor)
            savedStacks = DefaultedList.of();
            for (Slot slot : slots)
                savedStacks.add(slot.getStack().copy());
            savedCursorStack = getCursorStack().copy();
            blockContentUpdates = true;
        }
        super.onSlotClick(slotId, clickData, actionType, player);
        blockContentUpdates = false;
        if (cancel) {
            if (actionType == SlotActionType.QUICK_CRAFT)
                // clear quick craft info
                endQuickCraft();
            DefaultedList<ItemStack> trackedStacks = ((ScreenHandlerAccessor) this).getTrackedStacks();
            for (int i = 0; i < savedStacks.size(); i++) {
                ItemStack stack = savedStacks.get(i);
                slots.get(i).setStack(stack);
                trackedStacks.set(i, stack);
            }
            setCursorStack(savedCursorStack);
            // resync everything
            sendInventoryPacket(player.playerScreenHandler, (ServerPlayerEntity) player);
            sendInventoryPacket(this, (ServerPlayerEntity) player);
            sendCursorStackUpdatePacket((ServerPlayerEntity) player);
        } else if (player instanceof ServerPlayerEntity)
            menuHandler.postSlotClick((ServerPlayerEntity) player, inventory);
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        if (player instanceof ServerPlayerEntity)
            menuHandler.onClose((ServerPlayerEntity) player, inventory);
    }
}
