package adudecalledleo.serversiding.menu.simple;

import adudecalledleo.serversiding.menu.MenuHandler;
import adudecalledleo.serversiding.menu.simple.button.Button;
import adudecalledleo.serversiding.menu.simple.button.Label;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleMenuHandler implements MenuHandler {
    private final int rows;
    private final BackgroundPainter backgroundPainter;

    private final IntArraySet slotsToRepaint;
    private final MenuState menuState;

    private static final class SlotObject {
        enum Type {
            BUTTON,
            LABEL
        }

        private final Type type;
        private final Button button;
        private final Label label;

        public SlotObject(Button button) {
            type = Type.BUTTON;
            this.button = button;
            label = null;
        }

        public SlotObject(Label label) {
            type = Type.LABEL;
            button = null;
            this.label = label;
        }
    }

    private final Int2ReferenceOpenHashMap<SlotObject> objects;

    public SimpleMenuHandler(int rows, @NotNull BackgroundPainter backgroundPainter) {
        this.rows = rows;
        this.backgroundPainter = backgroundPainter;

        objects = new Int2ReferenceOpenHashMap<>();
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
        objects.put(slot, new SlotObject(button));
    }

    public void addLabel(int slot, @NotNull Label label) {
        objects.put(slot, new SlotObject(label));
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    private void repaint(ServerPlayerEntity player, Inventory inventory) {
        for (int slotId = 0; slotId < inventory.size(); slotId++) {
            if (!slotsToRepaint.contains(slotId))
                continue;
            if (objects.containsKey(slotId)) {
                SlotObject object = objects.get(slotId);
                switch (object.type) {
                case BUTTON:
                    inventory.setStack(slotId, object.button.getStack());
                    break;
                case LABEL:
                    inventory.setStack(slotId, object.label.getStack());
                    break;
                default:
                    break;
                }
            } else
                inventory.setStack(slotId, backgroundPainter.paint(slotId));
        }
        slotsToRepaint.clear();
    }

    @Override
    public void onOpen(ServerPlayerEntity player, Inventory inventory) {
        menuState.markAllSlotsForRepaint();
        repaint(player, inventory);
    }

    @Override
    public boolean onSlotClick(int slotId, int clickData, SlotActionType actionType, ServerPlayerEntity player, Inventory inventory) {
        menuState.closeAndDoThis = null;
        if (objects.containsKey(slotId)) {
            SlotObject object = objects.get(slotId);
            switch (object.type) {
            case BUTTON:
                object.button.onClick(menuState);
                break;
            default:
                break;
            }
        }
        if (menuState.closeAndDoThis != null) {
            player.closeHandledScreen();
            final Consumer<ServerPlayerEntity> consumer = menuState.closeAndDoThis;
            player.getServerWorld().getServer().execute(() -> consumer.accept(player));
        } else {
            slotsToRepaint.add(slotId);
            repaint(player, inventory);
        }
        return true;
    }

    @Override
    public void onClose(ServerPlayerEntity player, Inventory inventory) { }
}
