package adudecalledleo.mcmail.api.message;

import adudecalledleo.mcmail.api.MailboxIdentifier;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public interface Message {
    @NotNull Sender getSender();
    @NotNull MailboxIdentifier getRecipient();
    @NotNull Instant getTimestamp();
    @NotNull MessageContents getContents();
    boolean isRead();
    void setRead(boolean read);
    boolean delete();
}
