package adudecalledleo.craftdown.mixin;

import net.minecraft.text.KeybindText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeybindText.class)
public interface KeybindTextAccessor {
    @Invoker @NotNull Text callGetTranslated();
}
