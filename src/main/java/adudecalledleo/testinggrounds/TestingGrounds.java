package adudecalledleo.testinggrounds;

import adudecalledleo.testinggrounds.block.ModBlocks;
import adudecalledleo.testinggrounds.block.entity.ModBlockEntities;
import adudecalledleo.testinggrounds.command.ModCommands;
import adudecalledleo.testinggrounds.item.ModItemGroups;
import adudecalledleo.testinggrounds.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TestingGrounds implements ModInitializer {
    public static final String MOD_ID = "testinggrounds";
    public static final String MOD_NAME = "Testing Grounds";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

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
