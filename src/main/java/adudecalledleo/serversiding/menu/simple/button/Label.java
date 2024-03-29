package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.serversiding.menu.simple.MenuState;
import adudecalledleo.serversiding.util.ItemStackBuilder;
import adudecalledleo.serversiding.util.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public final class Label implements Button {
    @FunctionalInterface
    public interface StackProvider {
        @NotNull ItemStack getStack();

        static @NotNull StackProvider simple(@NotNull Item icon, @NotNull Text title, @NotNull Text... description) {
            return () -> ItemStackBuilder.create()
                    .setItem(icon)
                    .setCustomName(title.shallowCopy()
                            .styled(style -> style.withColor(Formatting.BLUE).withBold(true).withItalic(false))
                    )
                    .addLore(TextUtils.toLore(description))
                    .build();
        }
    }

    public static @NotNull Label of(@NotNull StackProvider stackProvider) {
        return new Label(stackProvider);
    }

    public static @NotNull Label of(@NotNull Item icon, @NotNull Text title, @NotNull Text... description) {
        return of(StackProvider.simple(icon, title, description));
    }

    private Label(StackProvider stackProvider) {
        this.stackProvider = stackProvider;
    }

    private final StackProvider stackProvider;

    @Override
    public @NotNull ItemStack getStack() {
        return stackProvider.getStack();
    }

    @Override
    public void onClick(@NotNull MenuState menuState) { }
}
