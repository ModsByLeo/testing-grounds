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

    private boolean blockContentUpdates;

    @Override
    public void sendContentUpdates() {
        if (!blockContentUpdates)
            super.sendContentUpdates();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean insertItem(PlayerEntity player, ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ServerPlayerEntity serverPlayer = null;
        if (player instanceof ServerPlayerEntity)
            serverPlayer = (ServerPlayerEntity) player;

        boolean bl = false;
        int i = startIndex;
        if (fromLast)
            i = endIndex - 1;

        Slot slot2;
        ItemStack itemStack;
        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (fromLast) {
                    if (i < startIndex)
                        break;
                } else if (i >= endIndex)
                    break;

                slot2 = slots.get(i);
                boolean canInsert = true;
                if (serverPlayer != null && i < 9 * rows)
                    canInsert = menuHandler.canInsert(i, stack, serverPlayer, inventory);
                itemStack = slot2.getStack();
                if (canInsert && !itemStack.isEmpty() && canStacksCombine(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot2.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount());
                        slot2.markDirty();
                        bl = true;
                    }
                }

                if (fromLast)
                    --i;
                else
                    ++i;
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast)
                i = endIndex - 1;
            else
                i = startIndex;

            while (true) {
                if (fromLast) {
                    if (i < startIndex)
                        break;
                } else if (i >= endIndex)
                    break;

                slot2 = slots.get(i);
                boolean canInsert = true;
                if (serverPlayer != null && i < 9 * rows)
                    canInsert = menuHandler.canInsert(i, stack, serverPlayer, inventory);
                itemStack = slot2.getStack();
                if (canInsert && itemStack.isEmpty()) {
                    if (stack.getCount() > slot2.getMaxItemCount())
                        slot2.setStack(stack.split(slot2.getMaxItemCount()));
                    else
                        slot2.setStack(stack.split(stack.getCount()));

                    slot2.markDirty();
                    bl = true;
                    break;
                }

                if (fromLast)
                    --i;
                else
                    ++i;
            }
        }

        return bl;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < 9 * rows) {
                if (!insertItem(player, itemStack2, 9 * rows, slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!insertItem(player, itemStack2, 0, 9 * rows, false))
                return ItemStack.EMPTY;

            if (itemStack2.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else
                slot.markDirty();
        }

        return itemStack;
    }

    private void sendInventoryPacket(ServerPlayerEntity player, ScreenHandler screenHandler) {
        player.networkHandler.sendPacket(new InventoryS2CPacket(screenHandler.syncId, screenHandler.getStacks()));
    }

    private void sendSlotUpdatePacket(ServerPlayerEntity player, int slotId) {
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, slotId, slots.get(slotId).getStack()));
    }

    private void sendCursorStackUpdatePacket(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, player.inventory.getCursorStack()));
    }

    @Override
    public ItemStack onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity player) {
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
                return ItemStack.EMPTY;
            }
            if (actionType == SlotActionType.CLONE) {
                // need to resync cursor stack
                sendCursorStackUpdatePacket((ServerPlayerEntity) player);
                return ItemStack.EMPTY;
            }
            if (actionType == SlotActionType.QUICK_CRAFT && ScreenHandler.unpackQuickCraftStage(clickData) < 2)
                // simple cancel (only do advanced cancel for QUICK_CRAFT after it's "finished")
                return ItemStack.EMPTY;
            if (actionType == SlotActionType.THROW || actionType == SlotActionType.PICKUP)
                // simple cancel (required for THROW, a nice optimization for PICKUP)
                return ItemStack.EMPTY;
        }
        DefaultedList<ItemStack> savedStacks = null;
        ItemStack savedCursorStack = ItemStack.EMPTY;
        if (cancel) {
            // QUICK_CRAFT, QUICK_MOVE or PICKUP_ALL - actions which change multiple slots
            // there are more efficient ways to do this, but for now let's just save and restore all slots (and cursor)
            savedStacks = DefaultedList.of();
            for (Slot slot : slots)
                savedStacks.add(slot.getStack().copy());
            savedCursorStack = player.inventory.getCursorStack();
            blockContentUpdates = true;
        }
        ItemStack ret = super.onSlotClick(slotId, clickData, actionType, player);
        blockContentUpdates = false;
        if (cancel) {
            if (actionType == SlotActionType.QUICK_CRAFT)
                // clear quick craft info
                endQuickCraft();
            ret = ItemStack.EMPTY;
            DefaultedList<ItemStack> trackedStacks = ((ScreenHandlerAccessor) this).getTrackedStacks();
            for (int i = 0; i < savedStacks.size(); i++) {
                ItemStack stack = savedStacks.get(i);
                slots.get(i).setStack(stack);
                trackedStacks.set(i, stack);
            }
            player.inventory.setCursorStack(savedCursorStack);
            // resync everything
            sendInventoryPacket((ServerPlayerEntity) player, player.playerScreenHandler);
            sendInventoryPacket((ServerPlayerEntity) player, this);
            sendCursorStackUpdatePacket((ServerPlayerEntity) player);
        }
        return ret;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        if (player instanceof ServerPlayerEntity)
            menuHandler.onClose((ServerPlayerEntity) player, inventory);
    }
}
