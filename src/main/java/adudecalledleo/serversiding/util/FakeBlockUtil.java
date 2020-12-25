package adudecalledleo.serversiding.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FakeBlockUtil {
    private FakeBlockUtil() { }
    
    public static void sendFakeBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos, @NotNull BlockState state,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        if (!player.notInAnyWorld && World.isValid(pos))
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, state), listener);
    }

    public static void sendFakeBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos, @NotNull BlockState state) {
        sendFakeBlock(player, pos, state, null);
    }

    public interface UpdatableBlockEntityType {
        @NotNull Identifier getRegistryId();
        int getUpdatePacketId();
    }

    public enum UpdatableBlockEntityTypes implements UpdatableBlockEntityType {
        MOB_SPAWNER(BlockEntityType.getId(BlockEntityType.MOB_SPAWNER), 1),
        COMMAND_BLOCK(BlockEntityType.getId(BlockEntityType.COMMAND_BLOCK), 2),
        BEACON(BlockEntityType.getId(BlockEntityType.BEACON), 3),
        SKULL(BlockEntityType.getId(BlockEntityType.SKULL), 4),
        CONDUIT(BlockEntityType.getId(BlockEntityType.CONDUIT), 5),
        BANNER(BlockEntityType.getId(BlockEntityType.BANNER), 6),
        STRUCTURE_BLOCK(BlockEntityType.getId(BlockEntityType.STRUCTURE_BLOCK), 7),
        END_GATEWAY(BlockEntityType.getId(BlockEntityType.END_GATEWAY), 8),
        SIGN(BlockEntityType.getId(BlockEntityType.SIGN), 9),
        // 10 is missing (possibly used to be Note Block?)
        BED(BlockEntityType.getId(BlockEntityType.BED), 11),
        JIGSAW_BLOCK(BlockEntityType.getId(BlockEntityType.JIGSAW), 12),
        CAMPFIRE(BlockEntityType.getId(BlockEntityType.CAMPFIRE), 13);

        private final Identifier registryId;
        private final int updatePacketId;

        UpdatableBlockEntityTypes(Identifier registryId, int updatePacketId) {
            this.registryId = registryId;
            this.updatePacketId = updatePacketId;
        }

        @Override
        public @NotNull Identifier getRegistryId() {
            return registryId;
        }

        @Override
        public int getUpdatePacketId() {
            return updatePacketId;
        }
    }

    private static final class CustomUpdatableBlockEntityType implements UpdatableBlockEntityType {
        private final Identifier registryId;

        public CustomUpdatableBlockEntityType(Identifier registryId) {
            this.registryId = registryId;
        }

        @Override
        public @NotNull Identifier getRegistryId() {
            return registryId;
        }

        @Override
        public int getUpdatePacketId() {
            return 127;
        }
    }

    private static final Object2ReferenceOpenHashMap<Identifier, UpdatableBlockEntityType> CUSTOM_TYPES =
            new Object2ReferenceOpenHashMap<>();

    /**
     * For use with block entities that implement {@link BlockEntityClientSerializable}.
     */
    public static UpdatableBlockEntityType customUpdatableType(Identifier registryId) {
        return CUSTOM_TYPES.computeIfAbsent(registryId, CustomUpdatableBlockEntityType::new);
    }

    private static void writeIdentifyingData(@NotNull BlockPos pos, @NotNull UpdatableBlockEntityType type,
            @NotNull CompoundTag tag) {
        tag.putString("id", type.getRegistryId().toString());
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableBlockEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendFakeBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @NotNull UpdatableBlockEntityType type, @NotNull CompoundTag tag,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        if (!player.notInAnyWorld && World.isValid(pos)) {
            writeIdentifyingData(pos, type, tag);
            player.networkHandler.sendPacket(new BlockEntityUpdateS2CPacket(pos, type.getUpdatePacketId(), tag), listener);
        }
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableBlockEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendFakeBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @NotNull UpdatableBlockEntityType type, @NotNull CompoundTag tag) {
        sendFakeBlockEntity(player, pos, type, tag, null);
    }

    public static void sendRealBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        sendFakeBlock(player, pos, player.getServerWorld().getBlockState(pos), listener);
    }

    public static void sendRealBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos) {
        sendRealBlock(player, pos, null);
    }
}
