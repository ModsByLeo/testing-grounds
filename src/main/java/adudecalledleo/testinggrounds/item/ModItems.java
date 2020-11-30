package adudecalledleo.testinggrounds.item;

import adudecalledleo.testinggrounds.block.ModBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static adudecalledleo.testinggrounds.TestingGrounds.id;

public final class ModItems {
    private ModItems() { }

    public static void register() { /* clinit */ }

    public static final BlockItem MAILBOX;

    static {
        MAILBOX = register(id("mailbox"), new BlockItem(ModBlocks.MAILBOX, baseMCMailSettings()));
    }

    private static <T extends Item> T register(Identifier id, T item) {
        return Registry.register(Registry.ITEM, id, item);
    }

    private static FabricItemSettings baseMCMailSettings() {
        return new FabricItemSettings().group(ModItemGroups.MCMAIL);
    }
}
