package adudecalledleo.mcmail.api;

import adudecalledleo.mcmail.api.message.MessageContents;
import adudecalledleo.mcmail.api.message.MessagePrerequisite;
import adudecalledleo.mcmail.api.message.Sender;
import adudecalledleo.mcmail.impl.MailboxProviderImpl;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MailboxProvider {
    static @NotNull MailboxProvider get() {
        MailboxProvider mailboxProvider = MailboxProviderImpl.getInstance();
        if (mailboxProvider == null)
            throw new IllegalStateException("Mailbox provider doesn't exist! Has the server not started yet/stopped?");
        return mailboxProvider;
    }

    void setPrerequisite(@NotNull MessagePrerequisite prerequisite);
    @NotNull Text describePrerequisite(@NotNull MessageContents contents);

    boolean hasMailbox(@NotNull MailboxIdentifier mId);
    @NotNull Optional<Mailbox> getMailbox(@NotNull MailboxIdentifier mId);
    @NotNull Mailbox getOrCreateMailbox(@NotNull MailboxIdentifier mId, @NotNull UUID ownerUuid);
    @NotNull Optional<Mailbox> removeMailbox(@NotNull MailboxIdentifier mId);

    @NotNull Set<Mailbox> queryOwnedBy(@NotNull UUID ownerUuid);

    default @NotNull Optional<Text> sendTo(@NotNull MailboxIdentifier mId,
            @NotNull Sender sender, @NotNull MessageContents contents) {
        Optional<Mailbox> mailbox = getMailbox(mId);
        return mailbox.map(value -> value.send(sender, contents))
                .orElseGet(() -> Optional.of(new TranslatableText("mcmail.message.error.bad_id")));
    }
}
