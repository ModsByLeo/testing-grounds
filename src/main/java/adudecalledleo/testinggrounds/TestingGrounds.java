package adudecalledleo.testinggrounds;

import adudecalledleo.testinggrounds.block.ModBlocks;
import adudecalledleo.testinggrounds.block.entity.ModBlockEntities;
import adudecalledleo.testinggrounds.command.ModCommands;
import adudecalledleo.testinggrounds.effect.ModStatusEffects;
import adudecalledleo.testinggrounds.item.ModItemGroups;
import adudecalledleo.testinggrounds.item.ModItems;
import adudecalledleo.testinggrounds.potion.ModPotions;
import net.fabricmc.api.ModInitializer;
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

        ModStatusEffects.register();
        ModPotions.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModItems.register();
        ModCommands.register();

        // DISABLED:preLaunch
        /*
        EntityDamageEvents.of(EntityType.CREEPER).registerAfter((target, source, amount) -> {
            // if damage won't kill creeper, ignite it
            if (amount < target.getHealth())
                target.ignite();
        });
        EntityDamageEvents.living().registerBefore((target, source, amount) -> {
            // if entity has Invincibility effect, cancel damage event
            if (target.hasStatusEffect(ModStatusEffects.INVINCIBILITY))
                return TriState.TRUE;
            return TriState.DEFAULT;
        });
        EntityDamageEvents.ofClass(TameableEntity.class).registerBefore((target, source, amount) -> {
            // don't let owners damage their own pets
            if (target.getOwnerUuid() != null && source.getAttacker() != null) {
                if (target.getOwnerUuid().equals(source.getAttacker().getUuid()))
                    return TriState.TRUE;
            }
            return TriState.DEFAULT;
        });
        EntityTickEvents.inTag(EntityTypeTags.ARROWS).registerAfter(entity -> {
            // make arrows a bit floaty
            if (!entity.isOnGround())
                entity.addVelocity(0, 0.1, 0);
        });*/
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
