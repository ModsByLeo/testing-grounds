package adudecalledleo.mcmail.api.message;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface that allows tying message sending to an arbitrary prerequisite.
 */
public interface MessagePrerequisite {
    @NotNull Text describe(@NotNull MessageContents contents);
    /**
     * @return empty {@code Optional} if can send, {@code Optional} containing reason if can't send
     */
    @NotNull Optional<Text> canSend(@NotNull UUID playerUuid, @NotNull MessageContents contents);
    void postSend(@NotNull UUID playerUuid, @NotNull MessageContents contents);

    MessagePrerequisite NONE = new MessagePrerequisite() {
        @Override
        public @NotNull Text describe(@NotNull MessageContents contents) {
            return new TranslatableText("mcmail.message.prerequisite.none");
        }

        @Override
        public @NotNull Optional<Text> canSend(@NotNull UUID playerUuid, @NotNull MessageContents contents) {
            return Optional.empty();
        }

        @Override
        public void postSend(@NotNull UUID playerUuid, @NotNull MessageContents contents) { }
    };
}
