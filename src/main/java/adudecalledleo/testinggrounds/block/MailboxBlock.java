package adudecalledleo.testinggrounds.block;

import adudecalledleo.lionutils.block.ProperHorizontalFacingBlock;
import adudecalledleo.testinggrounds.block.entity.MailboxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.Objects;
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
}
