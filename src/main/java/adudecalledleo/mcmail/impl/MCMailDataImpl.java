package adudecalledleo.mcmail.impl;

import adudecalledleo.lionutils.serialize.NbtUtil;
import adudecalledleo.mcmail.api.Message;
import adudecalledleo.mcmail.api.MCMailData;
import adudecalledleo.mcmail.api.Mailbox;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class MCMailDataImpl extends PersistentState implements MCMailData {
    private static final String KEY = "mcmail_data";

    public MCMailDataImpl() {
        super(KEY);
    }

    public static MCMailData get(ServerWorld world) {
        return world.getPersistentStateManager().get(MCMailDataImpl::new, KEY);
    }

    public static MCMailData getOrCreate(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(MCMailDataImpl::new, KEY);
    }

    private final class MailboxImpl implements Mailbox {
        private boolean tracked = false;

        private final UUID ownerUUID;
        private final BlockPos worldPos;
        private Text label;

        private MailboxImpl(UUID ownerUUID, BlockPos worldPos) {
            this.ownerUUID = ownerUUID;
            this.worldPos = worldPos;
        }

        @Override
        public UUID getOwnerUUID() {
            return ownerUUID;
        }

        @Override
        public BlockPos getWorldPos() {
            return worldPos;
        }

        @Override
        public Text getLabel() {
            return label;
        }

        @Override
        public void setLabel(Text label) {
            this.label = label;
            if (tracked)
                markDirty();
        }

        @Override
        public boolean trySend(Message message) {
            if (!tracked)
                return false;
            // TODO IMPLEMENT THIS!!!
            return false;
        }

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putUuid("owner_uuid", ownerUUID);
            NbtUtil.putBlockPos(tag, "world_pos", worldPos);
            tag.putString("label", Text.Serializer.toJson(label));
            return tag;
        }
    }

    private void deserializeAndAddMailbox(CompoundTag source) {
        if (!source.containsUuid("owner_uuid"))
            return;
        UUID ownerUUID = source.getUuid("owner_uuid");
        if (!NbtUtil.containsBlockPos(source, "world_pos"))
            return;
        BlockPos worldPos = NbtUtil.getBlockPos(source, "world_pos");
        if (!source.contains("label", NbtType.STRING))
            return;
        Text label = Text.Serializer.fromJson(source.getString("label"));

        MailboxImpl mailbox = new MailboxImpl(ownerUUID, worldPos);
        mailbox.label = label;
        addMailbox(mailbox);
    }

    private final Object2ReferenceOpenHashMap<BlockPos, MailboxImpl> byPos
            = new Object2ReferenceOpenHashMap<>();
    private final Object2ReferenceOpenHashMap<UUID, ReferenceArraySet<MailboxImpl>> byUUID
            = new Object2ReferenceOpenHashMap<>();

    @Override
    public Set<Mailbox> getMailboxesOf(UUID ownerUUID) {
        if (!byUUID.containsKey(ownerUUID))
            return Collections.emptySet();
        return Collections.unmodifiableSet(byUUID.get(ownerUUID));
    }

    @Override
    public Mailbox getMailbox(BlockPos worldPos) {
        return byPos.get(worldPos);
    }

    @Override
    public Mailbox getOrCreateMailbox(UUID ownerUUID, BlockPos worldPos) {
        MailboxImpl mailbox = byPos.get(worldPos);
        if (mailbox == null) {
            mailbox = new MailboxImpl(ownerUUID, worldPos);
            addMailbox(mailbox);
        }
        return mailbox;
    }

    private void addMailbox(MailboxImpl mailbox) {
        byPos.put(mailbox.worldPos, mailbox);
        byUUID.computeIfAbsent(mailbox.ownerUUID, uuid -> new ReferenceArraySet<>()).add(mailbox);
        mailbox.tracked = true;
        markDirty();
    }

    @Override
    public Mailbox removeMailbox(BlockPos worldPos) {
        MailboxImpl mailbox = byPos.remove(worldPos);
        if (mailbox == null)
            return null;
        ReferenceArraySet<MailboxImpl> set = byUUID.get(mailbox.ownerUUID);
        set.remove(mailbox);
        if (set.isEmpty())
            byUUID.remove(mailbox.ownerUUID);
        mailbox.tracked = false;
        markDirty();
        return mailbox;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag mailboxesList = tag.getList("mailboxes", NbtType.COMPOUND);
        if (mailboxesList == null)
            return;
        for (int i = 0; i < mailboxesList.size(); i++) {
            CompoundTag mailboxTag = mailboxesList.getCompound(i);
            deserializeAndAddMailbox(mailboxTag);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag mailboxesList = new ListTag();
        for (MailboxImpl mailbox : byPos.values())
            mailboxesList.add(mailbox.serialize());
        tag.put("mailboxes", mailboxesList);
        return tag;
    }
}
