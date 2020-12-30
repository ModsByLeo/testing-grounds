package adudecalledleo.serversiding.menu.simple;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public final class MenuState {
    private static final Consumer<ServerPlayerEntity> NO_OP = player -> { };

    private final IntSet allSlots;
    private final IntSet slotsToRepaint;
    Consumer<ServerPlayerEntity> closeAndDoThis;

    MenuState(IntSet allSlots, IntSet slotsToRepaint) {
        this.allSlots = allSlots;
        this.slotsToRepaint = slotsToRepaint;
    }

    public void markSlotForRepaint(int slot) {
        slotsToRepaint.add(slot);
    }

    public void markSlotsForRepaint(int... slots) {
        for (int slot : slots)
            markSlotForRepaint(slot);
    }

    public void markAllSlotsForRepaint() {
        slotsToRepaint.addAll(allSlots);
    }

    public void closeAndDo(Consumer<ServerPlayerEntity> runnable) {
        closeAndDoThis = runnable;
    }

    public void close() {
        closeAndDo(NO_OP);
    }
}
