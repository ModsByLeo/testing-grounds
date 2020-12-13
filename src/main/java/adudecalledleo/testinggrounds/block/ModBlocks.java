package adudecalledleo.testinggrounds.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static adudecalledleo.testinggrounds.TestingGrounds.id;

public final class ModBlocks {
    private ModBlocks() { }

    public static void register() { /* clinit */ }

    public static final MailboxBlock MAILBOX;

    static {
        MAILBOX = register(id("mailbox"),
                new MailboxBlock(FabricBlockSettings.of(Material.STONE)
                        .strength(3.5f, 3600000.0f)
                        .breakByTool(FabricToolTags.PICKAXES)));
    }

    private static <T extends Block> T register(Identifier id, T block) {
        return Registry.register(Registry.BLOCK, id, block);
    }
}
