package adudecalledleo.mcmail.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Mailbox {
    @NotNull MailboxIdentifier getId();
    @NotNull UUID getOwnerUuid();
}
