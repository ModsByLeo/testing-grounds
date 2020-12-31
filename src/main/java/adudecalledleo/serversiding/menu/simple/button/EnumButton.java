package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import adudecalledleo.serversiding.menu.simple.MenuState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EnumButton<V extends Enum<V>> implements Button {
    public static <V extends Enum<V>> @NotNull Button of(@NotNull EnumButton.StateAccessor<V> stateAccessor,
            @NotNull EnumButton.StackProvider<V> stackProvider) {
        return new EnumButton<>(stateAccessor, stackProvider);
    }

    @FunctionalInterface
    public interface ValueGetter<V extends Enum<V>> {
        @NotNull V getValue();
    }

    @FunctionalInterface
    public interface ValueSetter<V extends Enum<V>> {
        void setValue(@NotNull V value, @NotNull MenuState menuState);
    }

    public interface StateAccessor<V extends Enum<V>> extends ValueGetter<V>, ValueSetter<V> {
        void cycleValue(@NotNull MenuState menuState);

        static <V extends Enum<V>> @NotNull StateAccessor<V> of(Class<V> type, ValueGetter<V> supplier, ValueSetter<V> consumer) {
            if (type.getEnumConstants() == null)
                throw new IllegalArgumentException(type + " is not an enum!");
            return new StateAccessor<V>() {
                private final V[] values = type.getEnumConstants();

                @Override
                public @NotNull V getValue() {
                    return supplier.getValue();
                }

                @Override
                public void setValue(@NotNull V value, @NotNull MenuState menuState) {
                    consumer.setValue(value, menuState);
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
                            .append(stateNameFunction.apply(state).shallowCopy()
                                        .styled(style -> style.withItalic(style.isItalic())))
                    )
                    .addLore(Stream.of(description)
                            .map(Text::shallowCopy)
                            .map(line -> line.styled(style -> style.withColor(Formatting.GRAY).withItalic(true)))
                            .collect(Collectors.toList()))
                    .build();
        }

        static <V extends Enum<V>> @NotNull StackProvider<V> simple(Function<V, Item> iconFunction,
                Text name, Text... description) {
            return simple(iconFunction, state -> new LiteralText(state.toString()), name, description);
        }
    }

    private final StateAccessor<V> stateAccessor;
    private final StackProvider<V> stackProvider;

    private EnumButton(StateAccessor<V> stateAccessor, StackProvider<V> stackProvider) {
        this.stateAccessor = stateAccessor;
        this.stackProvider = stackProvider;
    }

    @Override
    public @NotNull ItemStack getStack() {
        return stackProvider.getStack(stateAccessor.getValue());
    }

    @Override
    public void onClick(@NotNull MenuState menuState) {
        stateAccessor.cycleValue(menuState);
    }
}
