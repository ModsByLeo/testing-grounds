package adudecalledleo.serversiding.impl;

import adudecalledleo.serversiding.input.SignPrompt;
import adudecalledleo.serversiding.util.FakeBlocks;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class SignPromptStorage {
    private static final Reference2ReferenceOpenHashMap<ServerPlayerEntity, Entry> ENTRIES =
            new Reference2ReferenceOpenHashMap<>();

    public static final class Entry {
        public final ServerPlayerEntity player;
        public final BlockPos pos;
        private final SignPrompt.Callback callback;
        private boolean called;

        public Entry(ServerPlayerEntity player, BlockPos pos, SignPrompt.Callback callback) {
            this.player = player;
            this.pos = pos;
            this.callback = callback;
            called = false;
        }

        private void sendRealBlock() {
            FakeBlocks.sendRealBlock(player, pos, future -> FakeBlocks.sendRealBlockEntity(player, pos));
        }

        public void succeed(Text[] lines) {
            if (!called) {
                called = true;
                callback.accept(SignPrompt.Result.success(lines));
                sendRealBlock();
            }
        }

        public void fail() {
            if (!called) {
                called = true;
                callback.accept(SignPrompt.Result.failure());
                sendRealBlock();
            }
        }
    }

    public static void add(ServerPlayerEntity player, BlockPos pos, SignPrompt.Callback callback) {
        ENTRIES.put(player, new Entry(player, pos.toImmutable(), callback));
    }

    public static Entry get(ServerPlayerEntity player) {
        return ENTRIES.get(player);
    }

    public static Entry remove(ServerPlayerEntity player) {
        return ENTRIES.remove(player);
    }
}
