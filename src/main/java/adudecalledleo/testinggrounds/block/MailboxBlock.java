package adudecalledleo.testinggrounds.block;

import adudecalledleo.lionutils.block.ProperHorizontalFacingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

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
}
