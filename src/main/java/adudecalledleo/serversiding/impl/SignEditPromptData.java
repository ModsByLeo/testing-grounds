package adudecalledleo.serversiding.impl;

import adudecalledleo.serversiding.util.SignEditPrompt;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SignEditPromptData {
    private static final Reference2ReferenceOpenHashMap<ServerPlayerEntity, Entry> ENTRIES =
            new Reference2ReferenceOpenHashMap<>();

    public static final class Entry {
        public final BlockPos pos;
        public final SignEditPrompt.Callback callback;

        public Entry(BlockPos pos, SignEditPrompt.Callback callback) {
            this.pos = pos;
            this.callback = callback;
        }
    }

    public static void add(ServerPlayerEntity player, BlockPos pos, SignEditPrompt.Callback callback) {
        ENTRIES.put(player, new Entry(pos.toImmutable(), callback));
    }

    public static Entry get(ServerPlayerEntity player) {
        return ENTRIES.get(player);
    }

    public static Entry remove(ServerPlayerEntity player) {
        return ENTRIES.remove(player);
    }
}
