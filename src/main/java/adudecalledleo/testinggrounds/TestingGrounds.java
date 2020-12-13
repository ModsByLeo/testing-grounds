package adudecalledleo.testinggrounds;

import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.testinggrounds.block.ModBlocks;
import adudecalledleo.testinggrounds.block.entity.ModBlockEntities;
import adudecalledleo.testinggrounds.command.ModCommands;
import adudecalledleo.testinggrounds.item.ModItemGroups;
import adudecalledleo.testinggrounds.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

public class TestingGrounds implements ModInitializer {
    public static final String MOD_ID = "testinggrounds";
    public static final String MOD_NAME = "Testing Grounds";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");

        ModBlocks.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModItems.register();
        ModCommands.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
