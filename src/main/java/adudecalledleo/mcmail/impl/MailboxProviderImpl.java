package adudecalledleo.mcmail.impl;

import adudecalledleo.mcmail.MCMail;
import adudecalledleo.mcmail.api.Mailbox;
import adudecalledleo.mcmail.api.MailboxIdentifier;
import adudecalledleo.mcmail.api.MailboxProvider;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MailboxProviderImpl implements MailboxProvider {
    private static MailboxProviderImpl instance;

    public static MailboxProvider getInstance() {
        return instance;
    }

    public static void onServerStarting(MinecraftServer server) {
        instance = new MailboxProviderImpl(server);
        instance.valid = true;
        instance.load();
    }

    public static void onServerStopped(MinecraftServer server) {
        if (instance.server == server) {
            instance.save();
            instance.valid = false;
            instance = null;
        }
    }

    private final MinecraftServer server;

    private boolean valid;

    private MailboxProviderImpl(MinecraftServer server) {
        this.server = server;
        valid = true;
    }

    private void assertValid() {
        if (!valid)
            throw new IllegalStateException("Provider is no longer valid! Has the server stopped?");
    }

    private final class MailboxImpl implements Mailbox {
        private final MailboxIdentifier mId;
        private final UUID ownerUuid;
        private boolean tracked;

        private MailboxImpl(MailboxIdentifier mId, UUID ownerUuid) {
            this.mId = mId;
            this.ownerUuid = ownerUuid;
        }

        @Override
        public @NotNull MailboxIdentifier getId() {
            return mId;
        }

        @Override
        public @NotNull UUID getOwnerUuid() {
            return ownerUuid;
        }
    }

    private final Reference2ReferenceOpenHashMap<MailboxIdentifier, MailboxImpl> mailboxes =
            new Reference2ReferenceOpenHashMap<>();

    private File getMailboxesFile() {
        return new File(server.getFile("mcmail"), "mailboxes.dat");
    }

    private void load() {
        CompoundTag mailboxesTag;
        try {
            mailboxesTag = NbtIo.readCompressed(getMailboxesFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not load mailboxes: failed to read file", e);
        }
        ListTag mailboxesList = mailboxesTag.getList("mailboxes", NbtType.COMPOUND);
        if (mailboxesList == null)
            throw new RuntimeException("Could not load mailboxes: malformed file (mailboxes list is missing or not a compound list)");
        for (int i = 0; i < mailboxesList.size(); i++) {
            CompoundTag mailboxTag = mailboxesList.getCompound(i);
            if (!mailboxTag.contains("id", NbtType.COMPOUND) || !mailboxTag.containsUuid("owner")) {
                MCMail.LOGGER.warn("Couldn't load mailbox " + i + ": malformed entry (id or owner missing)");
                continue;
            }
            MailboxIdentifier mId = MailboxIdentifier.fromTag(mailboxTag.getCompound("id"));
            if (mId == null) {
                MCMail.LOGGER.warn("Couldn't load mailbox " + i + ": malformed entry (id tag malformed)");
                continue;
            }
            UUID ownerUuid = mailboxTag.getUuid("owner");
            MailboxImpl mailbox = new MailboxImpl(mId, ownerUuid);
            mailbox.tracked = true;
            mailboxes.put(mId, mailbox);
        }
    }

    private void save() {
        ListTag mailboxesList = new ListTag();
        for (MailboxImpl mailbox : mailboxes.values()) {
            CompoundTag mailboxTag = new CompoundTag();
            mailboxTag.put("id", mailbox.getId().toTag(new CompoundTag()));
            mailboxTag.putUuid("owner", mailbox.getOwnerUuid());
            mailboxesList.add(mailboxTag);
        }
        CompoundTag mailboxesTag = new CompoundTag();
        mailboxesTag.put("mailboxes", mailboxesList);
        try {
            NbtIo.writeCompressed(mailboxesTag, getMailboxesFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not save mailboxes: failed to write file", e);
        }
    }

    @Override
    public boolean hasMailbox(MailboxIdentifier mId) {
        assertValid();
        return mailboxes.containsKey(mId);
    }

    @Override
    public @NotNull Optional<Mailbox> getMailbox(MailboxIdentifier mId) {
        assertValid();
        return Optional.ofNullable(mailboxes.get(mId));
    }

    @Override
    public @NotNull Mailbox getOrCreateMailbox(MailboxIdentifier mId, UUID ownerUuid) {
        assertValid();
        return mailboxes.computeIfAbsent(mId, mId1 -> {
            MailboxImpl mailbox = new MailboxImpl(mId, ownerUuid);
            mailbox.tracked = true;
            return mailbox;
        });
    }

    @Override
    public @NotNull Optional<Mailbox> removeMailbox(MailboxIdentifier mId) {
        assertValid();
        return Optional.ofNullable(mailboxes.remove(mId));
    }

    @Override
    public @NotNull Set<Mailbox> queryOwnedBy(UUID ownerUuid) {
        assertValid();
        Set<MailboxImpl> filtered = mailboxes.values().stream()
                .filter(mailbox -> mailbox.ownerUuid.equals(ownerUuid))
                .collect(Collectors.toSet());
        if (filtered.isEmpty())
            return Collections.emptySet();
        else
            return ImmutableSet.copyOf(filtered);
    }
}
