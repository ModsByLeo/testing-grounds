package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import adudecalledleo.serversiding.menu.simple.MenuState;
import adudecalledleo.serversiding.util.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class EnumButton<V extends Enum<V>> implements Button {
    public static <V extends Enum<V>> @NotNull Button of(@NotNull EnumButton.ValueAccessor<V> valueAccessor,
            @NotNull EnumButton.StackProvider<V> stackProvider) {
        return new EnumButton<>(valueAccessor, stackProvider);
    }

    @FunctionalInterface
    public interface ValueGetter<V extends Enum<V>> {
        @NotNull V getValue();
    }

    @FunctionalInterface
    public interface ValueSetter<V extends Enum<V>> {
        void setValue(@NotNull V value, @NotNull MenuState menuState);
    }

    public interface ValueAccessor<V extends Enum<V>> extends ValueGetter<V>, ValueSetter<V> {
        void cycleValue(@NotNull MenuState menuState);

        static <V extends Enum<V>> @NotNull ValueAccessor<V> of(@NotNull Class<V> type,
                @NotNull ValueGetter<V> getter, @NotNull ValueSetter<V> setter) {
            if (type.getEnumConstants() == null)
                throw new IllegalArgumentException(type + " is not an enum!");
            return new ValueAccessor<V>() {
                private final V[] values = type.getEnumConstants();

                @Override
                public @NotNull V getValue() {
                    return getter.getValue();
                }

                @Override
                public void setValue(@NotNull V value, @NotNull MenuState menuState) {
                    setter.setValue(value, menuState);
                }

                @Override
                public void cycleValue(@NotNull MenuState menuState) {
                    setValue(values[(getValue().ordinal() + 1) % values.length], menuState);
                }
            };
        }
    }

    @FunctionalInterface
    public interface StackProvider<V extends Enum<V>> {
        @NotNull ItemStack getStack(@NotNull V state);

        static <V extends Enum<V>> @NotNull StackProvider<V> simple(Function<V, Item> iconFunction, Function<V, Text> stateNameFunction,
                Text name, Text... description) {
            return state -> ItemStackBuilder.create()
                    .setItem(iconFunction.apply(state))
                    .setCustomName(
                            name.shallowCopy()
                                    .styled(style -> style.withColor(Formatting.BLUE).withBold(true).withItalic(false))
                            .append(new LiteralText(": ")
                                     .styled(style -> style.withColor(Formatting.GRAY).withBold(false).withItalic(false)))
                            .append(TextUtils.ensureItalicSet(stateNameFunction.apply(state).shallowCopy()))
                    )
                    .addLore(TextUtils.toLore(description))
                    .build();
        }

        static <V extends Enum<V>> @NotNull StackProvider<V> simple(Function<V, Item> iconFunction,
                Text name, Text... description) {
            return simple(iconFunction, state -> new LiteralText(state.toString())
                    .styled(style -> style.withColor(Formatting.WHITE).withBold(true)),
                    name, description);
        }
    }

    private final ValueAccessor<V> valueAccessor;
    private final StackProvider<V> stackProvider;

    private EnumButton(ValueAccessor<V> valueAccessor, StackProvider<V> stackProvider) {
        this.valueAccessor = valueAccessor;
        this.stackProvider = stackProvider;
    }

    @Override
    public @NotNull ItemStack getStack() {
        return stackProvider.getStack(valueAccessor.getValue());
    }

    @Override
    public void onClick(@NotNull MenuState menuState) {
        valueAccessor.cycleValue(menuState);
    }
}
