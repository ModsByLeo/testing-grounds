package adudecalledleo.craftdown.mixin;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(TranslatableText.class)
public interface TranslatableTextAccessor {
    @Invoker void callUpdateTranslations();
    @Accessor @NotNull List<StringVisitable> getTranslations();
}
