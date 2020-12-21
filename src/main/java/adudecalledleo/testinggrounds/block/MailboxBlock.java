package adudecalledleo.testinggrounds.block;

import adudecalledleo.lionutils.block.ProperHorizontalFacingBlock;
import adudecalledleo.mcmail.api.MailboxIdentifier;
import adudecalledleo.mcmail.api.MailboxProvider;
import adudecalledleo.testinggrounds.block.entity.MailboxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class MailboxBlock extends ProperHorizontalFacingBlock {
    public static final BooleanProperty FLAG_UP = BooleanProperty.of("flag_up");

    public MailboxBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FLAG_UP, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FLAG_UP);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (state.get(FLAG_UP))
            return 0.0f; // don't allow breaking mailboxes that still have messages in them
        if (!player.isCreativeLevelTwoOp()) {
            UUID ownerUuid = getOwnerUuid(world, pos);
            if (!Objects.equals(player.getUuid(), ownerUuid))
                return 0.0f; // don't allow players to break mailboxes that don't belong to them
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    private UUID getOwnerUuid(BlockView world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MailboxBlockEntity)
            return ((MailboxBlockEntity) be).getOwnerUuid();
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack itemStack) {
        if (!world.isClient)
            world.getBlockTickScheduler().schedule(pos, this, 2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.getBlockTickScheduler().schedule(pos, this, 1500); // do again in 30 seconds
        MailboxProvider.get().getMailbox(MailboxIdentifier.of(world.getRegistryKey(), pos))
                .ifPresent(mailbox -> world.setBlockState(pos, state.with(FLAG_UP,
                        mailbox.getMessages().stream().anyMatch(message -> !message.isRead())),
                        3));
    }
}
