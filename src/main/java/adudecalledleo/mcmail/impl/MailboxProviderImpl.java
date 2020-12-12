package adudecalledleo.mcmail.impl;

import adudecalledleo.mcmail.api.Mailbox;
import adudecalledleo.mcmail.api.MailboxIdentifier;
import adudecalledleo.mcmail.api.MailboxProvider;
import adudecalledleo.mcmail.api.message.Message;
import adudecalledleo.mcmail.api.message.MessageContents;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceBigArrayBigList;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static adudecalledleo.mcmail.MCMail.LOGGER;

public final class MailboxProviderImpl implements MailboxProvider {
    private static final long MAILBOXES_VERSION = 0;
    private static final long MESSAGES_VERSION = 0;

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
        private Text label;
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

        @Override
        public boolean send(UUID senderUuid, MessageContents contents) {
            MessageImpl message = new MessageImpl(senderUuid, mId, Instant.now(), contents);
            message.read = false;
            addMessage(message);
            return true;
        }

        @Override
        public @NotNull List<Message> getMessages() {
            if (!messages.containsKey(mId))
                return Collections.emptyList();
            return ImmutableList.copyOf(messages.get(mId));
        }

        @Override
        public @NotNull Optional<Text> getLabel() {
            return Optional.ofNullable(label);
        }

        @Override
        public void setLabel(Text label) {
            this.label = label.shallowCopy();
        }
    }

    private final class MessageImpl implements Message {
        private final UUID senderUuid;
        private final MailboxIdentifier recipient;
        private final Instant timestamp;
        private final MessageContents contents;
        private boolean read;

        private MessageImpl(UUID senderUuid, MailboxIdentifier recipient, Instant timestamp,
                MessageContents contents) {
            this.senderUuid = senderUuid;
            this.recipient = recipient;
            this.timestamp = timestamp;
            this.contents = contents;
        }

        @Override
        public @NotNull UUID getSenderUuid() {
            return senderUuid;
        }

        @Override
        public @NotNull MailboxIdentifier getRecipient() {
            return recipient;
        }

        @Override
        public @NotNull Instant getTimestamp() {
            return timestamp;
        }

        @Override
        public @NotNull MessageContents getContents() {
            return contents;
        }

        @Override
        public boolean isRead() {
            return read;
        }

        @Override
        public void setRead(boolean read) {
            this.read = read;
        }

        @Override
        public boolean delete() {
            return removeMessage(this);
        }
    }

    private final Reference2ReferenceOpenHashMap<MailboxIdentifier, MailboxImpl> mailboxes =
            new Reference2ReferenceOpenHashMap<>();
    private final Reference2ReferenceOpenHashMap<MailboxIdentifier, ReferenceBigArrayBigList<MessageImpl>> messages =
            new Reference2ReferenceOpenHashMap<>();

    private File getDataPath() {
        return server.getFile("mcmail");
    }

    private File getMailboxesFile() {
        return new File(getDataPath(), "mailboxes.dat");
    }

    private File getMessagesFile() {
        return new File(getDataPath(), "messages.dat");
    }

    private void load() {
        loadMailboxes();
        loadMessages();
    }
    
    private void loadMailboxes() {
        LOGGER.info("Loading mailboxes...");
        CompoundTag mailboxesTag;
        try {
            mailboxesTag = NbtIo.readCompressed(getMailboxesFile());
        } catch (FileNotFoundException e) {
            LOGGER.info("Mailboxes file not found, assuming new installation.");
            return;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load mailboxes: failed to read file", e);
        }
        if (!mailboxesTag.contains("mailboxes", NbtType.LIST))
            throw new RuntimeException("Couldn't load mailboxes: malformed file (mailboxes list is missing)");
        ListTag mailboxesList = mailboxesTag.getList("mailboxes", NbtType.COMPOUND);
        for (int i = 0; i < mailboxesList.size(); i++) {
            CompoundTag mailboxTag = mailboxesList.getCompound(i);
            if (!mailboxTag.contains("id", NbtType.COMPOUND) || !mailboxTag.containsUuid("owner")) {
                LOGGER.warn("Couldn't load mailbox " + i + ": malformed entry (missing fields)");
                continue;
            }
            MailboxIdentifier mId = MailboxIdentifier.fromTag(mailboxTag.getCompound("id"));
            if (mId == null) {
                LOGGER.warn("Couldn't load mailbox " + i + ": malformed entry (id tag malformed)");
                continue;
            }
            UUID ownerUuid = mailboxTag.getUuid("owner");
            Text label = null;
            if (mailboxTag.contains("label", NbtType.STRING))
                label = Text.Serializer.fromJson(mailboxTag.getString("label"));
            MailboxImpl mailbox = new MailboxImpl(mId, ownerUuid);
            mailbox.label = label;
            mailbox.tracked = true;
            mailboxes.put(mId, mailbox);
        }
        LOGGER.info("Successfully loaded {} mailboxes.", mailboxes.size());
    }

    private void loadMessages() {
        LOGGER.info("Loading messages...");
        CompoundTag messagesTag;
        try {
            messagesTag = NbtIo.readCompressed(getMessagesFile());
        } catch (FileNotFoundException e) {
            LOGGER.info("Messages file not found, assuming new installation.");
            return;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load messages: failed to read file", e);
        }
        if (!messagesTag.contains("messages", NbtType.LIST))
            throw new RuntimeException("Couldn't load messages: malformed file (messages list is missing)");
        ListTag messagesList = messagesTag.getList("messages", NbtType.COMPOUND);
        for (int i = 0; i < messagesList.size(); i++) {
            CompoundTag messageTag = messagesList.getCompound(i);
            if (!messageTag.containsUuid("sender") || !messageTag.contains("recipient", NbtType.COMPOUND)
                    || !messageTag.contains("timestamp", NbtType.LONG) || !messageTag.contains("contents", NbtType.COMPOUND)) {
                LOGGER.warn("Couldn't load message " + i + ": malformed entry (missing fields)");
                continue;
            }
            UUID senderUuid = messageTag.getUuid("sender");
            MailboxIdentifier recipient = MailboxIdentifier.fromTag(messageTag.getCompound("recipient"));
            if (recipient == null) {
                LOGGER.warn("Couldn't load message " + i + ": malformed entry (malformed recipient)");
                continue;
            }
            if (!mailboxes.containsKey(recipient)) {
                LOGGER.warn("Couldn't load message " + i + ": orphaned entry (recipient doesn't exist)");
                continue;
            }
            Instant timestamp = Instant.ofEpochSecond(messageTag.getLong("timestamp"));
            MessageContents contents = MessageContents.fromTag(messageTag.getCompound("contents"));
            if (contents == null) {
                LOGGER.warn("Couldn't load message " + i + ": malformed entry (malformed contents)");
                continue;
            }
            MessageImpl message = new MessageImpl(senderUuid, recipient, timestamp, contents);
            boolean read = false;
            if (messageTag.contains("read", NbtType.BYTE))
                read = messageTag.getBoolean("read");
            message.read = read;
            addMessage(message);
        }
        long count = messages.values().stream().mapToLong(ReferenceBigArrayBigList::size64).sum();
        LOGGER.info("Successfully loaded {} messages.", count);
    }

    private void save() {
        saveMailboxes();
        saveMessages();
    }

    private void saveMailboxes() {
        LOGGER.info("Saving mailboxes...");
        CompoundTag mailboxesTag = new CompoundTag();
        mailboxesTag.putLong("version", MAILBOXES_VERSION);
        ListTag mailboxesList = new ListTag();
        for (MailboxImpl mailbox : mailboxes.values()) {
            CompoundTag mailboxTag = new CompoundTag();
            mailboxTag.put("id", mailbox.mId.toTag(new CompoundTag()));
            mailboxTag.putUuid("owner", mailbox.ownerUuid);
            mailboxTag.putString("label", Text.Serializer.toJson(mailbox.label));
            mailboxesList.add(mailboxTag);
        }
        mailboxesTag.put("mailboxes", mailboxesList);
        try {
            NbtIo.writeCompressed(mailboxesTag, getMailboxesFile());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save mailboxes: failed to write file", e);
        }
        LOGGER.info("Successfully saved mailboxes.");
    }

    private void saveMessages() {
        LOGGER.info("Saving messages...");
        CompoundTag messagesTag = new CompoundTag();
        messagesTag.putLong("version", MESSAGES_VERSION);
        ListTag messagesList = new ListTag();
        for (ReferenceBigArrayBigList<MessageImpl> sublist : messages.values()) {
            for (MessageImpl message : sublist) {
                CompoundTag messageTag = new CompoundTag();
                messageTag.putUuid("sender", message.senderUuid);
                messageTag.put("recipient", message.recipient.toTag(new CompoundTag()));
                messageTag.putLong("timestamp", message.timestamp.getEpochSecond());
                messageTag.put("contents", message.contents.toTag(new CompoundTag()));
                messageTag.putBoolean("read", message.read);
                messagesList.add(messageTag);
            }
        }
        messagesTag.put("messages", messagesList);
        try {
            NbtIo.writeCompressed(messagesTag, getMessagesFile());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save messages: failed to write file", e);
        }
        LOGGER.info("Successfully saved messages.");
    }

    private void addMessage(MessageImpl message) {
        messages.computeIfAbsent(message.recipient, mailboxIdentifier -> new ReferenceBigArrayBigList<>())
                .add(message);
    }

    private boolean removeMessage(MessageImpl message) {
        if (!messages.containsKey(message.recipient))
            return false;
        ReferenceBigArrayBigList<MessageImpl> messageList = messages.get(message.recipient);
        boolean succ = messageList.remove(message);
        if (messageList.isEmpty()) {
            messages.remove(message.recipient, messageList);
            return true;
        }
        return succ;
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
