package adudecalledleo.mcmail.api;
import adudecalledleo.mcmail.impl.MCMailDataImpl;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.UUID;

public interface MCMailData {
    static MCMailData get(ServerWorld world) {
        return MCMailDataImpl.get(world);
    }

    static MCMailData getOrCreate(ServerWorld world) {
        return MCMailDataImpl.getOrCreate(world);
    }

    Set<Mailbox> getMailboxesOf(UUID ownerUUID);
    Mailbox getMailbox(BlockPos worldPos);
    Mailbox getOrCreateMailbox(UUID ownerUUID, BlockPos worldPos);
    Mailbox removeMailbox(BlockPos worldPos);
}
