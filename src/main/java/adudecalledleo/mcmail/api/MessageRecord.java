package adudecalledleo.mcmail.api;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;

import java.time.Instant;
import java.util.UUID;

public final class MessageRecord {
    private final Instant timestamp;
    private final UUID senderUUID;
    private final UUID receiverUUID;
    private final Message message;

    public static final MessageRecord EMPTY;

    static {
        UUID nilUUID = new UUID(0, 0);
        EMPTY = new MessageRecord(Instant.EPOCH, nilUUID, nilUUID, Message.EMPTY);
    }

    public MessageRecord(Instant timestamp, UUID senderUUID, UUID receiverUUID, Message message) {
        this.timestamp = timestamp;
        this.senderUUID = senderUUID;
        this.receiverUUID = receiverUUID;
        this.message = message;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public UUID getReceiverUUID() {
        return receiverUUID;
    }

    public Message getLetter() {
        return message;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("timestamp", timestamp.getEpochSecond());
        tag.putUuid("sender_uuid", senderUUID);
        tag.putUuid("receiver_uuid", receiverUUID);
        tag.put("message", message.serialize());
        return tag;
    }

    public static MessageRecord deserialize(CompoundTag source) {
        if (!source.contains("timestamp", NbtType.LONG))
            return EMPTY;
        Instant timestamp = Instant.ofEpochSecond(source.getLong("timestamp"));
        if (!source.containsUuid("sender_uuid"))
            return EMPTY;
        UUID senderUUID = source.getUuid("sender_uuid");
        if (!source.containsUuid("receiver_uuid"))
            return EMPTY;
        UUID receiverUUID = source.getUuid("receiver_uuid");
        Message message = Message.deserialize(source.getCompound("message"));
        if (message == Message.EMPTY)
            return EMPTY;
        return new MessageRecord(timestamp, senderUUID, receiverUUID, message);
    }
}
