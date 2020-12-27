package adudecalledleo.serversiding.impl;

import adudecalledleo.serversiding.util.FakeBlockUtil;
import adudecalledleo.serversiding.util.SignEditPrompt;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SignEditPromptData {
    private static final Reference2ReferenceOpenHashMap<ServerPlayerEntity, Entry> ENTRIES =
            new Reference2ReferenceOpenHashMap<>();

    public static final class Entry {
        public final ServerPlayerEntity player;
        public final BlockPos pos;
        private final SignEditPrompt.Callback callback;
        private boolean called;

        public Entry(ServerPlayerEntity player, BlockPos pos, SignEditPrompt.Callback callback) {
            this.player = player;
            this.pos = pos;
            this.callback = callback;
            called = false;
        }

        private void sendRealBlock() {
            FakeBlockUtil.sendRealBlock(player, pos, future -> FakeBlockUtil.sendRealBlockEntity(player, pos));
        }

        public void succeed(Text[] lines) {
            if (!called) {
                called = true;
                callback.accept(SignEditPrompt.Result.success(lines));
                sendRealBlock();
            }
        }

        public void fail() {
            if (!called) {
                called = true;
                callback.accept(SignEditPrompt.Result.failure());
                sendRealBlock();
            }
        }
    }

    public static void add(ServerPlayerEntity player, BlockPos pos, SignEditPrompt.Callback callback) {
        ENTRIES.put(player, new Entry(player, pos.toImmutable(), callback));
    }

    public static Entry get(ServerPlayerEntity player) {
        return ENTRIES.get(player);
    }

    public static Entry remove(ServerPlayerEntity player) {
        return ENTRIES.remove(player);
    }
}
