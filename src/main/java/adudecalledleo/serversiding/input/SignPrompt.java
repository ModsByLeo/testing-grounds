package adudecalledleo.serversiding.input;

import adudecalledleo.serversiding.impl.SignPromptStorage;
import adudecalledleo.serversiding.util.FakeBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class SignPrompt {
    public static final class Result {
        private final boolean successful;
        private final Text[] lines;

        private Result(boolean successful, Text[] lines) {
            this.successful = successful;
            this.lines = lines;
        }

        public static Result success(Text[] lines) {
            return new Result(true, lines);
        }

        private static final Result FAILURE = new Result(false, new Text[0]);

        public static Result failure() {
            return FAILURE;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public @NotNull Text[] getLines() {
            return lines;
        }

        public int getLineCount() {
            return lines.length;
        }

        public @NotNull Text getLine(int i) {
            return lines[i];
        }
    }

    @FunctionalInterface
    public interface Callback {
        void accept(@NotNull Result result);
    }

    public enum Type {
        OAK(Blocks.OAK_SIGN.getDefaultState()),
        SPRUCE(Blocks.SPRUCE_SIGN.getDefaultState()),
        BIRCH(Blocks.BIRCH_SIGN.getDefaultState()),
        ACACIA(Blocks.ACACIA_SIGN.getDefaultState()),
        JUNGLE(Blocks.JUNGLE_SIGN.getDefaultState()),
        DARK_OAK(Blocks.DARK_OAK_SIGN.getDefaultState());

        private final BlockState blockState;

        Type(BlockState blockState) {
            this.blockState = blockState;
        }

        public BlockState getBlockState() {
            return blockState;
        }
    }

    public static void open(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos, @NotNull Callback callback,
            @NotNull SignPrompt.Type type, @NotNull DyeColor textColor, @NotNull Text... initialLines) {
        SignPromptStorage.Entry entry = SignPromptStorage.remove(player);
        if (entry != null)
            entry.fail();

        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 4; i++) {
            tag.putString("Text" + (i + 1),
                    i < initialLines.length
                            ? Text.Serializer.toJson(initialLines[i])
                            : "");
        }
        tag.putString("Color", textColor.getName());

        FakeBlocks.sendFakeBlock(player, pos, type.getBlockState(), future ->
                FakeBlocks.sendFakeBlockEntity(player, pos, FakeBlocks.UpdatableEntityTypes.SIGN, tag,
                        future1 -> player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(pos), future2 ->
                                SignPromptStorage.add(player, pos, callback))));
    }
}
