package adudecalledleo.serversiding.menu.simple;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BackgroundPainter {
    @NotNull ItemStack paint(int slotId);

    static BackgroundPainter fill(Item item) {
        return slotId -> ItemStackBuilder.create()
                .setItem(item)
                .setCustomName(LiteralText.EMPTY)
                .build();
    }

    BackgroundPainter EMPTY = slotId -> ItemStack.EMPTY;
}
