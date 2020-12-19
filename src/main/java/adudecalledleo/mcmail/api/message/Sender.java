package adudecalledleo.mcmail.api.message;

import adudecalledleo.lionutils.network.GameProfileUtil;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static adudecalledleo.mcmail.MCMail.NIL_UUID;

public abstract class Sender {
    protected final String type;

    protected Sender(String type) {
        this.type = type;
    }

    public abstract boolean isPlayer();
    public abstract @NotNull UUID getUuid();
    /**
     * Name is only available for non-player senders.<br>
     * Use {@link #getSenderName(Sender)} to get a player sender's name.
     */
    public abstract @NotNull Optional<Text> getName();
    public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
        tag.putString("type", type);
        return tag;
    }

    public static @NotNull Sender ofPlayer(@NotNull UUID playerUuid) {
        return new PlayerSender(playerUuid);
    }

    public static @NotNull Sender ofNPC(@NotNull Text npcName) {
        return new NPCSender(npcName);
    }

    public static Sender fromTag(CompoundTag tag) {
        if (tag == null)
            return null;
        if (!tag.contains("type", NbtType.STRING))
            return null;
        switch (tag.getString("type")) {
        case "player":
            if (!tag.containsUuid("uuid"))
                return null;
            return ofPlayer(tag.getUuid("uuid"));
        case "npc":
            if (!tag.contains("name", NbtType.STRING))
                return null;
            Text npcName = Text.Serializer.fromJson(tag.getString("name"));
            if (npcName == null)
                return null;
            return ofNPC(npcName);
        default:
            return null;
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

    private static final class PlayerSender extends Sender {
        private final UUID playerUuid;

        public PlayerSender(UUID playerUuid) {
            super("player");
            this.playerUuid = playerUuid;
        }

        @Override
        public boolean isPlayer() {
            return true;
        }

        @Override
        public @NotNull UUID getUuid() {
            return playerUuid;
        }

        @Override
        public @NotNull Optional<Text> getName() {
            return Optional.empty();
        }

        @Override
        public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
            super.toTag(tag);
            tag.putUuid("uuid", playerUuid);
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlayerSender that = (PlayerSender) o;
            return type.equals(that.type) && playerUuid.equals(that.playerUuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, playerUuid);
        }

        @Override
        public String toString() {
            return "Sender.Player{" + playerUuid + "}";
        }
    }

    private static final class NPCSender extends Sender {
        private final Text npcName;

        public NPCSender(Text npcName) {
            super("npc");
            this.npcName = npcName;
        }

        @Override
        public boolean isPlayer() {
            return false;
        }

        @Override
        public @NotNull UUID getUuid() {
            return NIL_UUID;
        }

        @Override
        public @NotNull Optional<Text> getName() {
            return Optional.of(npcName);
        }

        @Override
        public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
            super.toTag(tag);
            tag.putString("name", Text.Serializer.toJson(npcName));
            return tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NPCSender that = (NPCSender) o;
            return type.equals(that.type) && npcName.equals(that.npcName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, npcName);
        }

        @Override
        public String toString() {
            return "Sender.NPC{" + npcName + "}";
        }
    }
}
