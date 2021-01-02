package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import adudecalledleo.serversiding.menu.simple.MenuState;
import adudecalledleo.serversiding.menu.simple.SimpleMenuHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RadioButtonGroup {
    public static @NotNull RadioButtonGroup of(@NotNull RadioButtonGroup.ChoiceAccessor valueAccessor,
            @NotNull ChoiceInfoProvider choiceInfoProvider) {
        return new RadioButtonGroup(valueAccessor, choiceInfoProvider);
    }

    @FunctionalInterface
    public interface ChoiceGetter {
        int getChoice();
    }

    @FunctionalInterface
    public interface ChoiceSetter {
        void setChoice(int choice, @NotNull MenuState menuState);
    }

    public interface ChoiceAccessor extends ChoiceGetter, ChoiceSetter {
        static @NotNull RadioButtonGroup.ChoiceAccessor of(@NotNull RadioButtonGroup.ChoiceGetter getter,
                @NotNull RadioButtonGroup.ChoiceSetter setter) {
            return new ChoiceAccessor() {
                @Override
                public int getChoice() {
                    return getter.getChoice();
                }

                @Override
                public void setChoice(int choice, @NotNull MenuState menuState) {
                    setter.setChoice(choice, menuState);
                }
            };
        }
    }

    public interface ChoiceInfoProvider {
        @NotNull Text getChoiceName(int choice);
        @NotNull Text[] getChoiceDescription(int choice);

        static @NotNull ChoiceInfoProvider of(IntFunction<Text> nameFunction, IntFunction<Text[]> descriptionFunction) {
            return new ChoiceInfoProvider() {
                @Override
                public @NotNull Text getChoiceName(int choice) {
                    return nameFunction.apply(choice);
                }

                @Override
                public @NotNull Text[] getChoiceDescription(int choice) {
                    return descriptionFunction.apply(choice);
                }
            };
        }
    }

    private final ChoiceAccessor choiceAccessor;
    private final ChoiceInfoProvider choiceInfoProvider;
    private final IntArrayList slots;

    private RadioButtonGroup(ChoiceAccessor choiceAccessor, ChoiceInfoProvider choiceInfoProvider) {
        this.choiceAccessor = choiceAccessor;
        this.choiceInfoProvider = choiceInfoProvider;
        slots = new IntArrayList();
    }

    public @NotNull RadioButtonGroup addChoiceAt(int slot) {
        slots.add(slot);
        return this;
    }

    public void addTo(@NotNull SimpleMenuHandler menu) {
        for (int i = 0; i < slots.size(); i++) {
            final int choice = i;
            menu.addButton(slots.getInt(i), ToggleButton.of(
                    ToggleButton.ValueAccessor.of(() -> choiceAccessor.getChoice() == choice,
                            (value, menuState) -> {
                        int lastChoice = choiceAccessor.getChoice();
                        if (lastChoice >= 0 && lastChoice < slots.size())
                            menuState.markSlotForRepaint(slots.getInt(lastChoice));
                        choiceAccessor.setChoice(choice, menuState);
                    }),
                    state -> {
                        MutableText name = choiceInfoProvider.getChoiceName(choice).shallowCopy()
                                .styled(style -> style.withColor(Formatting.BLUE).withBold(true).withItalic(false));
                        if (state)
                            name.append(new LiteralText(" (selected)")
                                    .styled(style -> style.withColor(Formatting.GREEN).withBold(true).withItalic(false)));
                        return ItemStackBuilder.create()
                                .setItem(state ? Items.LIME_DYE : Items.GRAY_DYE)
                                .setCustomName(name)
                                .addLore(Stream.of(choiceInfoProvider.getChoiceDescription(choice))
                                        .map(Text::shallowCopy)
                                        .map(line -> line
                                                .styled(style -> style.withColor(Formatting.GRAY).withItalic(true)))
                                        .collect(Collectors.toList()))
                                .build();
                    }
            ));
        }
    }
}
