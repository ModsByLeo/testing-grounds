package adudecalledleo.serversiding.menu.simple;

import adudecalledleo.serversiding.menu.MenuHandler;
import adudecalledleo.serversiding.menu.simple.button.Button;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleMenuHandler implements MenuHandler {
    private final int rows;
    private final BackgroundPainter backgroundPainter;

    private final IntArraySet slotsToRepaint;
    private final MenuState menuState;

    private final Int2ReferenceOpenHashMap<Button> buttons;
    private final Int2ReferenceOpenHashMap<SlotListener> slotListeners;

    public SimpleMenuHandler(int rows, @NotNull BackgroundPainter backgroundPainter) {
        this.rows = rows;
        this.backgroundPainter = backgroundPainter;

        buttons = new Int2ReferenceOpenHashMap<>();
        slotListeners = new Int2ReferenceOpenHashMap<>();

        IntArraySet allSlots = new IntArraySet();
        for (int i = 0; i < 9 * rows; i++)
            allSlots.add(i);
        slotsToRepaint = new IntArraySet();
        menuState = new MenuState(allSlots, slotsToRepaint);
    }

    public SimpleMenuHandler(int rows, @NotNull Item backgroundItem) {
        this(rows, BackgroundPainter.fill(backgroundItem));
    }

    public SimpleMenuHandler(int rows) {
        this(rows, BackgroundPainter.EMPTY);
    }

    public void addButton(int slot, @NotNull Button button) {
        slotListeners.remove(slot);
        buttons.put(slot, button);
    }

    public void addSlotListener(int slot, @NotNull SlotListener slotListener) {
        buttons.remove(slot);
        slotListeners.put(slot, slotListener);
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    private void repaint(Inventory inventory) {
        for (int slotId = 0; slotId < inventory.size(); slotId++) {
            if (!slotsToRepaint.contains(slotId))
                continue;
            if (buttons.containsKey(slotId))
                inventory.setStack(slotId, buttons.get(slotId).getStack());
            else if (!slotListeners.containsKey(slotId))
                inventory.setStack(slotId, backgroundPainter.paint(slotId));
        }
        slotsToRepaint.clear();
    }

    @Override
    public void onOpen(ServerPlayerEntity player, Inventory inventory) {
        menuState.markAllSlotsForRepaint();
        repaint(inventory);
    }

    private void applyMenuState(int slotId, ServerPlayerEntity player, Inventory inventory) {
        if (menuState.closeAndDoThis != null) {
            player.closeHandledScreen();
            final Consumer<ServerPlayerEntity> consumer = menuState.closeAndDoThis;
            player.getServerWorld().getServer().execute(() -> consumer.accept(player));
        } else {
            slotsToRepaint.add(slotId);
            repaint(inventory);
        }
    }

    @Override
    public boolean onSlotClick(int slotId, int clickData, SlotActionType actionType, ServerPlayerEntity player, Inventory inventory) {
        if (actionType == SlotActionType.PICKUP) {
            menuState.closeAndDoThis = null;
            if (buttons.containsKey(slotId)) {
                buttons.get(slotId).onClick(menuState);
                applyMenuState(slotId, player, inventory);
            }
        }
        return !slotListeners.containsKey(slotId);
    }

    @Override
    public void postSlotClick(int slotId, int clickData, SlotActionType actionType, ServerPlayerEntity player, Inventory inventory) {
        menuState.closeAndDoThis = null;
        if (slotListeners.containsKey(slotId)) {
            slotListeners.get(slotId).onChanged(slotId, player, inventory, menuState);
            applyMenuState(slotId, player, inventory);
        }
    }

    @Override
    public boolean canInsert(int slotId, ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
        if (slotListeners.containsKey(slotId))
            return slotListeners.get(slotId).canInsert(slotId, stack, player, inventory);
        return false;
    }

    @Override
    public boolean canExtract(int slotId, ServerPlayerEntity player, Inventory inventory) {
        if (slotListeners.containsKey(slotId))
            return slotListeners.get(slotId).canExtract(slotId, player, inventory);
        return false;
    }

    @Override
    public int getMaxItemCount(int slotId, ServerPlayerEntity player, Inventory inventory) {
        if (slotListeners.containsKey(slotId))
            return slotListeners.get(slotId).getMaxItemCount(slotId, player, inventory);
        return 64;
    }

    @Override
    public int getMaxItemCount(int slotId, ItemStack stack, ServerPlayerEntity player, Inventory inventory) {
        if (slotListeners.containsKey(slotId))
            return slotListeners.get(slotId).getMaxItemCount(slotId, stack, player, inventory);
        return 64;
    }

    @Override
    public void onClose(ServerPlayerEntity player, Inventory inventory) {
        for (Int2ReferenceMap.Entry<SlotListener> entry : slotListeners.int2ReferenceEntrySet()) {
            if (entry.getValue().doDropOnClose(entry.getIntKey(), player, inventory))
                player.inventory.offerOrDrop(player.getServerWorld(), inventory.getStack(entry.getIntKey()));
        }
    }
}
