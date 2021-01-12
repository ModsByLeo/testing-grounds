package adudecalledleo.testinggrounds;

import adudecalledleo.entitydamageevent.api.EntityDamageEvent;
import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.testinggrounds.block.ModBlocks;
import adudecalledleo.testinggrounds.block.entity.ModBlockEntities;
import adudecalledleo.testinggrounds.command.ModCommands;
import adudecalledleo.testinggrounds.effect.ModStatusEffects;
import adudecalledleo.testinggrounds.item.ModItemGroups;
import adudecalledleo.testinggrounds.item.ModItems;
import adudecalledleo.testinggrounds.potion.ModPotions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

public class TestingGrounds implements ModInitializer {
    public static final String MOD_ID = "testinggrounds";
    public static final String MOD_NAME = "Testing Grounds";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");

        ModStatusEffects.register();
        ModPotions.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModItems.register();
        ModCommands.register();

        EntityDamageEvent.of(EntityType.ZOMBIE).register((target, source, amount) ->
                target.hasStatusEffect(ModStatusEffects.INVINCIBILITY)
                        ? TriState.TRUE
                        : TriState.DEFAULT);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
