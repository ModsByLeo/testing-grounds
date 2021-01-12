package adudecalledleo.testinggrounds.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class ModStatusEffect extends StatusEffect {
    // stupid protected constructor *grumble grumble*
    public ModStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }
}
