package adudecalledleo.testinggrounds.potion;

import adudecalledleo.testinggrounds.effect.ModStatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

import static adudecalledleo.testinggrounds.TestingGrounds.id;

public final class ModPotions {
    private ModPotions() { }

    public static final Potion INVINCIBILITY = new Potion(new StatusEffectInstance(ModStatusEffects.INVINCIBILITY, 1200));

    public static void register() {
        Registry.register(Registry.POTION, id("invincibility"), INVINCIBILITY);
    }
}
