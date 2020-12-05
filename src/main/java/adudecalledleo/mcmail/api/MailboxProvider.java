package adudecalledleo.mcmail.api;

import adudecalledleo.mcmail.impl.MailboxProviderImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MailboxProvider {
    static MailboxProvider get() {
        MailboxProvider mailboxProvider = MailboxProviderImpl.getInstance();
        if (mailboxProvider == null)
            throw new IllegalStateException("Mailbox provider doesn't exist! Has the server not started yet/stopped?");
        return mailboxProvider;
    }

    boolean hasMailbox(MailboxIdentifier mId);
    @NotNull Optional<Mailbox> getMailbox(MailboxIdentifier mId);
    @NotNull Mailbox getOrCreateMailbox(MailboxIdentifier mId, UUID ownerUuid);
    @NotNull Optional<Mailbox> removeMailbox(MailboxIdentifier mId);

    @NotNull Set<Mailbox> queryOwnedBy(UUID ownerUuid);
}
