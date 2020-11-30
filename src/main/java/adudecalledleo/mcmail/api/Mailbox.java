package adudecalledleo.mcmail.api;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface Mailbox {
    UUID getOwnerUUID();
    BlockPos getWorldPos();

    Text getLabel();
    void setLabel(Text label);

    boolean trySend(ItemStack letterStack);
}
