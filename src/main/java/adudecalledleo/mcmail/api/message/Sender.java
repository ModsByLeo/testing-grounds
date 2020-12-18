package adudecalledleo.mcmail.api.message;

import adudecalledleo.lionutils.network.GameProfileUtil;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

import static adudecalledleo.mcmail.MCMail.NIL_UUID;

public final class Sender {
    public static @NotNull Sender ofPlayer(@NotNull UUID playerUuid) {
        return new Sender(playerUuid);
    }

    public static @NotNull Sender ofNPC(@NotNull Text npcName) {
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

    /**
     * Gets the specified sender's name.<p>
     * If the sender is not a player, it simply retrieves the sender's {@linkplain #getName() name field}.<br>
     * Otherwise, it queries the session service for the player's current username (using {@link GameProfileUtil#getPlayerName(UUID)}.
     * @param sender the sender
     * @return the sender's name as a {@code Text}
     */
    public static @NotNull Text getSenderName(Sender sender) {
        Text unknown = new TranslatableText("mcmail.message.sender.unknown");
        if (!sender.isPlayer())
            return sender.getName().orElse(unknown);
        String playerName = GameProfileUtil.getPlayerName(sender.getUuid());
        if (GameProfileUtil.PLAYER_NAME_UNKNOWN.equals(playerName))
            return unknown;
        return new LiteralText(playerName);
    }

    private final boolean isPlayer;
    private final UUID uuid;
    private final Text name;

    private Sender(UUID uuid) {
        isPlayer = true;
        this.uuid = uuid;
        name = null;
    }

    private Sender(Text name) {
        isPlayer = false;
        uuid = NIL_UUID;
        this.name = name;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    /**
     * Name is only available for non-player senders.<br>
     * Use {@link #getSenderName(Sender)} to get a player sender's name.
     */
    public @NotNull Optional<Text> getName() {
        return Optional.ofNullable(name);
    }

    public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
        if (isPlayer) {
            tag.putBoolean("player", true);
            tag.putUuid("uuid", uuid);
        } else {
            tag.putBoolean("player", false);
            tag.putString("name", Text.Serializer.toJson(name));
        }
        return tag;
    }
}
