package adudecalledleo.mcmail.api;

import adudecalledleo.mcmail.api.message.Message;
import adudecalledleo.mcmail.api.message.MessageContents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Mailbox {
    @NotNull MailboxIdentifier getId();
    @NotNull UUID getOwnerUuid();

    /**
     * @return empty {@code Optional} on success, {@code Optional} containing error message on failure
     */
    @NotNull Optional<Text> send(@NotNull UUID senderUuid, @NotNull MessageContents contents);
    @NotNull List<Message> getMessages();

    @NotNull Optional<Text> getLabel();
    void setLabel(Text label);
}
