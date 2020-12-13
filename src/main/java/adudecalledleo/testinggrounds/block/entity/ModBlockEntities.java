package adudecalledleo.testinggrounds.block.entity;

import adudecalledleo.testinggrounds.TestingGrounds;
import adudecalledleo.testinggrounds.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public final class ModBlockEntities {
    private ModBlockEntities() { }

    public static void register() { /* clinit */ }

    public static final BlockEntityType<MailboxBlockEntity> MAILBOX;

    static {
        MAILBOX = register(TestingGrounds.id("mailbox"), MailboxBlockEntity::new, ModBlocks.MAILBOX);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(Identifier id, Supplier<T> supplier, Block... blocks) {
       return Registry.register(Registry.BLOCK_ENTITY_TYPE, id,
               BlockEntityType.Builder.create(supplier, blocks).build(null));
    }
}
