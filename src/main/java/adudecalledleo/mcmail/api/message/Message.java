package adudecalledleo.mcmail.api.message;

import adudecalledleo.mcmail.api.MailboxIdentifier;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

public interface Message {
    @NotNull UUID getSenderUuid();
    @NotNull MailboxIdentifier getRecipient();
    @NotNull Instant getTimestamp();
    @NotNull MessageContents getContents();
    boolean isRead();
    void setRead(boolean read);
    boolean delete();
}
