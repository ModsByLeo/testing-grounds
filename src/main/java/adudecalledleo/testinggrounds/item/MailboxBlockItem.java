package adudecalledleo.testinggrounds.item;

import adudecalledleo.testinggrounds.block.ModBlocks;
import adudecalledleo.testinggrounds.block.entity.MailboxBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MailboxBlockItem extends BlockItem {
    public MailboxBlockItem(Settings settings) {
        super(ModBlocks.MAILBOX, settings);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack,
            BlockState state) {
        if (world.isClient)
            return true;
        if (player == null)
            return false;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MailboxBlockEntity) {
            ((MailboxBlockEntity) be).setOwnerUuid(player.getUuid());
            return true;
        }
        return false;
    }
}
