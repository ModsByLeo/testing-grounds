package adudecalledleo.testinggrounds.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public class MailboxBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private UUID ownerUuid;

    public MailboxBlockEntity() {
        super(ModBlockEntities.MAILBOX);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        if (!Objects.equals(this.ownerUuid, ownerUuid)) {
            markDirty();
            if (world != null && !world.isClient)
                sync();
        }
        this.ownerUuid = ownerUuid;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        fromClientTag(tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        return toClientTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        if (!tag.containsUuid("owner"))
            ownerUuid = null;
        else
            ownerUuid = tag.getUuid("owner");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putUuid("owner", ownerUuid);
        return tag;
    }
}
