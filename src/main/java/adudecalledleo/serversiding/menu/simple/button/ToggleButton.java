package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.serversiding.menu.simple.MenuState;
import adudecalledleo.serversiding.util.ItemStackBuilder;
import adudecalledleo.serversiding.util.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public final class ToggleButton implements Button {
    public static @NotNull Button of(@NotNull ToggleButton.ValueAccessor valueAccessor, @NotNull StackProvider stackProvider) {
        return new ToggleButton(valueAccessor, stackProvider);
    }

    @FunctionalInterface
    public interface ValueGetter {
        boolean getValue();
    }

    @FunctionalInterface
    public interface ValueSetter {
        void setValue(boolean value, @NotNull MenuState menuState);
    }

    public interface ValueAccessor extends ValueGetter, ValueSetter {
        default void toggleValue(@NotNull MenuState menuState) {
            setValue(!getValue(), menuState);
        }

        static @NotNull ToggleButton.ValueAccessor of(@NotNull ValueGetter getter, @NotNull ValueSetter setter) {
            return new ValueAccessor() {
                @Override
                public boolean getValue() {
                    return getter.getValue();
                }

                @Override
                public void setValue(boolean value, @NotNull MenuState menuState) {
                    setter.setValue(value, menuState);
                }
            };
        }
    }

    @FunctionalInterface
    public interface StackProvider {
        @NotNull ItemStack getStack(boolean state);

        static @NotNull StackProvider simple(Text name, Text... description) {
            return state -> ItemStackBuilder.create()
                    .setItem(state ? Items.LIME_DYE : Items.GRAY_DYE)
                    .setCustomName(
                            name.shallowCopy()
                                    .styled(style -> style.withColor(Formatting.BLUE).withBold(true).withItalic(false))
                            .append(new LiteralText(": ")
                                    .styled(style -> style.withColor(Formatting.GRAY).withBold(false).withItalic(false))
                            .append(new TranslatableText(state ? "options.on" : "options.off")
                                    .styled(style -> style.withColor(state ? Formatting.GREEN : Formatting.RED)
                                            .withBold(true).withItalic(false))))
                    )
                    .addLore(TextUtils.toLore(description))
                    .build();
        }
    }

    private final ValueAccessor valueAccessor;
    private final StackProvider stackProvider;

    private ToggleButton(ValueAccessor valueAccessor, StackProvider stackProvider) {
        this.valueAccessor = valueAccessor;
        this.stackProvider = stackProvider;
    }

    @Override
    public @NotNull ItemStack getStack() {
        return stackProvider.getStack(valueAccessor.getValue());
    }

    @Override
    public void onClick(@NotNull MenuState menuState) {
        valueAccessor.toggleValue(menuState);
    }
}
