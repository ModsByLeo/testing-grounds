package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.serversiding.menu.simple.MenuState;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Button {
    @NotNull ItemStack getStack();
    void onClick(@NotNull MenuState menuState);

    static @NotNull Button of(@NotNull Consumer<MenuState> onClick, @NotNull ItemStack stack) {
        return new Button() {
            @Override
            public @NotNull ItemStack getStack() {
                return stack;
            }

            @Override
            public void onClick(@NotNull MenuState menuState) {
                onClick.accept(menuState);
            }
        };
    }
}
