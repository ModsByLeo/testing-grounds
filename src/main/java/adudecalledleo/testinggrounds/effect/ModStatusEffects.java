package adudecalledleo.testinggrounds.effect;

import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

import static adudecalledleo.testinggrounds.TestingGrounds.id;

public final class ModStatusEffects {
    private ModStatusEffects() { }

    public static final ModStatusEffect INVINCIBILITY = new ModStatusEffect(StatusEffectType.BENEFICIAL, 0x7073B4);

    public static void register() {
        Registry.register(Registry.STATUS_EFFECT, id("invincibility"), INVINCIBILITY);
    }
}
