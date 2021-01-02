package adudecalledleo.serversiding.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FakeBlocks {
    private FakeBlocks() { }
    
    public static void sendFakeBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos, @NotNull BlockState state,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        if (!player.notInAnyWorld && World.isValid(pos))
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, state), listener);
    }

    public static void sendFakeBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos, @NotNull BlockState state) {
        sendFakeBlock(player, pos, state, null);
    }

    public interface UpdatableEntityType {
        @NotNull Identifier getRegistryId();
        int getUpdatePacketId();
    }

    public enum UpdatableEntityTypes implements UpdatableEntityType {
        MOB_SPAWNER(BlockEntityType.MOB_SPAWNER, 1),
        COMMAND_BLOCK(BlockEntityType.COMMAND_BLOCK, 2),
        BEACON(BlockEntityType.BEACON, 3),
        SKULL(BlockEntityType.SKULL, 4),
        CONDUIT(BlockEntityType.CONDUIT, 5),
        BANNER(BlockEntityType.BANNER, 6),
        STRUCTURE_BLOCK(BlockEntityType.STRUCTURE_BLOCK, 7),
        END_GATEWAY(BlockEntityType.END_GATEWAY, 8),
        SIGN(BlockEntityType.SIGN, 9),
        // 10 is missing (possibly used to be Note Block?)
        BED(BlockEntityType.BED, 11),
        JIGSAW_BLOCK(BlockEntityType.JIGSAW, 12),
        CAMPFIRE(BlockEntityType.CAMPFIRE, 13);

        private final Identifier registryId;
        private final int updatePacketId;

        UpdatableEntityTypes(BlockEntityType<?> blockEntityType, int updatePacketId) {
            registryId = BlockEntityType.getId(blockEntityType);
            this.updatePacketId = updatePacketId;

            if (registryId == null)
                throw new RuntimeException("Vanilla block entity type \"" + name() + "\" is not registered?!");
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

    private static final class CustomUpdatableEntityType implements UpdatableEntityType {
        private final Identifier registryId;

        public CustomUpdatableEntityType(Identifier registryId) {
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

    private static final Object2ReferenceOpenHashMap<Identifier, UpdatableEntityType> CUSTOM_TYPES =
            new Object2ReferenceOpenHashMap<>();

    /**
     * For use with block entities that implement {@link BlockEntityClientSerializable}.
     */
    public static UpdatableEntityType customUpdatableType(@NotNull Identifier registryId) {
        return CUSTOM_TYPES.computeIfAbsent(registryId, CustomUpdatableEntityType::new);
    }

    private static void writeIdentifyingData(@NotNull BlockPos pos, @NotNull FakeBlocks.UpdatableEntityType type,
            @NotNull CompoundTag tag) {
        tag.putString("id", type.getRegistryId().toString());
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendFakeBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @NotNull FakeBlocks.UpdatableEntityType type, @NotNull CompoundTag tag,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        if (!player.notInAnyWorld && World.isValid(pos)) {
            CompoundTag copy = tag.copy();
            writeIdentifyingData(pos, type, copy);
            player.networkHandler.sendPacket(new BlockEntityUpdateS2CPacket(pos, type.getUpdatePacketId(), copy), listener);
        }
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendFakeBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @NotNull FakeBlocks.UpdatableEntityType type, @NotNull CompoundTag tag) {
        sendFakeBlockEntity(player, pos, type, tag, null);
    }

    public static void sendRealBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        sendFakeBlock(player, pos, player.getServerWorld().getBlockState(pos), listener);
    }

    public static void sendRealBlock(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos) {
        sendRealBlock(player, pos, null);
    }

    private static @Nullable Packet<?> getUpdatePacket(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos) {
        BlockEntity blockEntity = player.getServerWorld().getBlockEntity(pos);
        if (blockEntity == null)
            return null;
        // force command blocks to create an update packet (custom "isDirty" logic here, thanks mojank)
        boolean neededUpdatePacket = false;
        CommandBlockBlockEntity commandBlockBlockEntity = null;
        if (blockEntity instanceof CommandBlockBlockEntity)
            commandBlockBlockEntity = (CommandBlockBlockEntity) blockEntity;
        if (commandBlockBlockEntity != null) {
            neededUpdatePacket = commandBlockBlockEntity.needsUpdatePacket();
            commandBlockBlockEntity.setNeedsUpdatePacket(true);
        }
        // call vanilla's toUpdatePacket() method (BlockEntityClientSerializable should also return an update packet here)
        Packet<?> packet = blockEntity.toUpdatePacket();
        // restore command block state
        if (commandBlockBlockEntity != null)
            commandBlockBlockEntity.setNeedsUpdatePacket(neededUpdatePacket);
        return packet;
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendRealBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos,
            @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        if (!player.notInAnyWorld && World.isValid(pos)) {
            Packet<?> packet = getUpdatePacket(player, pos);
            if (packet != null)
                player.networkHandler.sendPacket(packet, listener);
        }
    }

    /**
     * This method only works with certain block entities that have special behavior for syncing with clients
     * (I.E. those specified in the {@link UpdatableEntityTypes} enum, or which implement {@link BlockEntityClientSerializable}).<br>
     * Other block entities rely on the chunk itself being sent to the player, which is beyond the scope of this utility library
     * (and also your mod, probably).
     */
    public static void sendRealBlockEntity(@NotNull ServerPlayerEntity player, @NotNull BlockPos pos) {
        sendRealBlock(player, pos, null);
    }
}
