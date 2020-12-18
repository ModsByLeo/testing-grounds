package adudecalledleo.mcmail.api.message;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static adudecalledleo.mcmail.MCMail.NIL_UUID;

public final class Sender {
    public static Sender ofPlayer(@NotNull UUID playerUuid) {
        return new Sender(playerUuid);
    }

    public static Sender ofNPC(@NotNull Text npcName) {
        return new Sender(npcName);
    }

    public static Sender fromTag(CompoundTag tag) {
        if (tag == null)
            return null;
        if (!tag.contains("player", NbtType.BYTE))
            return null;
        boolean isPlayer = tag.getBoolean("player");
        if (isPlayer) {
            if (!tag.containsUuid("uuid"))
                return null;
            return ofPlayer(tag.getUuid("uuid"));
        } else {
            if (!tag.contains("name", NbtType.STRING))
                return null;
            //noinspection ConstantConditions
            return ofNPC(Text.Serializer.fromJson(tag.getString("name")));
        }
    }

    private final boolean isPlayer;
    private final UUID playerUuid;
    private final Text npcName;

    private Sender(UUID playerUuid) {
        isPlayer = true;
        this.playerUuid = playerUuid;
        npcName = null;
    }

    private Sender(Text npcName) {
        isPlayer = false;
        playerUuid = NIL_UUID;
        this.npcName = npcName;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public @NotNull UUID getUuid() {
        return playerUuid;
    }

    public @NotNull Optional<Text> getName() {
        return Optional.ofNullable(npcName);
    }

    public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
        if (isPlayer) {
            tag.putBoolean("player", true);
            tag.putUuid("uuid", playerUuid);
        } else {
            tag.putBoolean("player", false);
            tag.putString("name", Text.Serializer.toJson(npcName));
        }
        return tag;
    }
}
