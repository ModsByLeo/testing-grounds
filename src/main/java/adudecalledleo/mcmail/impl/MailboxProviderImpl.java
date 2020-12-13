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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static adudecalledleo.mcmail.MCMail.LOGGER;
import static adudecalledleo.mcmail.MCMail.NIL_UUID;

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
        public @NotNull Optional<Text> send(@NotNull UUID senderUuid, @NotNull MessageContents contents) {
            if (!tracked)
                return Optional.of(new TranslatableText("mcmail.message.error.bad_mailbox"));
            if (NIL_UUID.equals(senderUuid))
                return Optional.of(new TranslatableText("mcmail.message.error.bad_sender"));
            if (contents.getInventory().size() > 27)
                return Optional.of(new TranslatableText("mcmail.message.error.inventory_too_big",
                        contents.getInventory().size(), 27));
            MessageImpl message = new MessageImpl(senderUuid, mId, Instant.now(), contents);
            message.read = false;
            addMessage(message);
            return Optional.empty();
        }

        @Override
        public @NotNull List<Message> getMessages() {
            if (!tracked || !messages.containsKey(mId))
                return Collections.emptyList();
            return ImmutableList.copyOf(messages.get(mId));
        }

        @Override
        public @NotNull Optional<Text> getLabel() {
            return Optional.ofNullable(label);
        }

        @Override
        public void setLabel(Text label) {
            if (label == null)
                this.label = null;
            else
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

    private Path getDataPath() {
        return server.getSavePath(WorldSavePath.ROOT).resolve("mcmail");
    }

    private Path getMailboxesPath() {
        return getDataPath().resolve("mailboxes.dat");
    }

    private Path getMessagesPath() {
        return getDataPath().resolve("messages.dat");
    }

    private void load() {
        loadMailboxes();
        loadMessages();
    }
    
    private void loadMailboxes() {
        Path mailboxesPath = getMailboxesPath();
        LOGGER.info("Loading mailboxes from \"{}\"...", mailboxesPath);
        CompoundTag mailboxesTag;
        try (InputStream is = Files.newInputStream(mailboxesPath)) {
            mailboxesTag = NbtIo.readCompressed(is);
        } catch (NoSuchFileException e) {
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
        Path messagesPath = getMessagesPath();
        LOGGER.info("Loading messages from \"{}\"...", messagesPath);
        CompoundTag messagesTag;
        try (InputStream is = Files.newInputStream(messagesPath)) {
            messagesTag = NbtIo.readCompressed(is);
        } catch (NoSuchFileException e) {
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
        try {
            Files.createDirectories(getDataPath());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save: failed to create data path", e);
        }
        saveMailboxes();
        saveMessages();
    }

    private void saveMailboxes() {
        Path mailboxesPath = getMailboxesPath();
        LOGGER.info("Saving mailboxes to \"{}\"...", mailboxesPath);
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
        try (OutputStream os = Files.newOutputStream(mailboxesPath)) {
            NbtIo.writeCompressed(mailboxesTag, os);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save mailboxes: failed to write file", e);
        }
        LOGGER.info("Successfully saved mailboxes.");
    }

    private void saveMessages() {
        Path messagesPath = getMessagesPath();
        LOGGER.info("Saving messages to \"{}\"...", messagesPath);
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
        try (OutputStream os = Files.newOutputStream(messagesPath)) {
            NbtIo.writeCompressed(messagesTag, os);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save messages: failed to write file", e);
        }
        LOGGER.info("Successfully saved messages.");
    }

    private void addMessage(MessageImpl message) {
        assertValid();
        messages.computeIfAbsent(message.recipient, mailboxIdentifier -> new ReferenceBigArrayBigList<>())
                .add(message);
    }

    private boolean removeMessage(MessageImpl message) {
        assertValid();
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
