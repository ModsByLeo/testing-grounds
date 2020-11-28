package adudecalledleo.testinggrounds.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import static adudecalledleo.testinggrounds.TestingGrounds.id;

public final class ModItemGroups {
    private ModItemGroups() { }

    public static void register() { /* clinit */ }

    public static final ItemGroup MCMAIL;

    static {
        MCMAIL = FabricItemGroupBuilder.build(id("mcmail"), () -> new ItemStack(ModItems.LETTER));
    }
}
