package adudecalledleo.testinggrounds.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ModItems {
    private ModItems() { }

    public static void register() { /* clinit */ }

    static {

    }

    private static <T extends Item> T register(Identifier id, T item) {
        return Registry.register(Registry.ITEM, id, item);
    }
}
