package adudecalledleo.testinggrounds.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public final class ModBlockEntities {
    private ModBlockEntities() { }

    public static void register() { /* clinit */ }

    static {

    }

    private static <T extends BlockEntity> BlockEntityType<T> register(Identifier id, Supplier<T> supplier, Block... blocks) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, id,
                BlockEntityType.Builder.create(supplier, blocks).build(null));
    }
}
