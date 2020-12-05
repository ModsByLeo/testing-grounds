package adudecalledleo.testinggrounds.block;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModBlocks {
    private ModBlocks() { }

    public static void register() { /* clinit */ }

    static {

    }

    private static <T extends Block> T register(Identifier id, T block) {
        return Registry.register(Registry.BLOCK, id, block);
    }
}
