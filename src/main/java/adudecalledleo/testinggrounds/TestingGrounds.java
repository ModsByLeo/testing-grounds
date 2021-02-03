package adudecalledleo.testinggrounds;

import adudecalledleo.entityevents.api.EntityDamageEvents;
import adudecalledleo.entityevents.api.EntityTickEvents;
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

        ModStatusEffects.register();
        ModPotions.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModItems.register();
        ModCommands.register();

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
        }).registerCancelled((target, source, amount) -> {
            if (target.getOwnerUuid() != null && source.getAttacker() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) source.getAttacker();
                if (target.getOwnerUuid().equals(player.getUuid()))
                    player.sendMessage(new LiteralText("Prevented own pet damage!"), true);
            }
        });
        EntityTickEvents.inTag(EntityTypeTags.ARROWS).registerAfter(entity -> {
            // make arrows a bit floatier
            if (!entity.isOnGround())
                entity.addVelocity(0, 0.025, 0);
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
