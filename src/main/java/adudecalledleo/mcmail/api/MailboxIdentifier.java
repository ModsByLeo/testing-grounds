package adudecalledleo.mcmail.api;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class MailboxIdentifier {
    private final RegistryKey<World> worldKey;
    private final BlockPos worldPos;

    private MailboxIdentifier(RegistryKey<World> worldKey, BlockPos worldPos) {
        this.worldKey = worldKey;
        this.worldPos = worldPos;
    }

    public static MailboxIdentifier of(@NotNull RegistryKey<World> worldKey, @NotNull BlockPos worldPos) {
        return new MailboxIdentifier(worldKey, worldPos);
    }

    public static MailboxIdentifier fromTag(@NotNull CompoundTag tag) {
        if (!tag.contains("dim", NbtType.STRING) || !tag.contains("pos", NbtType.LONG))
            return null;
        Identifier worldKeyValue = Identifier.tryParse(tag.getString("dim"));
        if (worldKeyValue == null)
            return null;
        return of(RegistryKey.of(Registry.DIMENSION, worldKeyValue), BlockPos.fromLong(tag.getLong("pos")));
    }

    @NotNull
    public RegistryKey<World> getWorldKey() {
        return worldKey;
    }

    @NotNull
    public BlockPos getWorldPos() {
        return worldPos;
    }

    public CompoundTag toTag(@NotNull CompoundTag tag) {
        tag.putString("dim", worldKey.getValue().toString());
        tag.putLong("pos", worldPos.asLong());
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MailboxIdentifier that = (MailboxIdentifier) o;
        return worldKey.equals(that.worldKey) && worldPos.equals(that.worldPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldKey, worldPos);
    }
}
