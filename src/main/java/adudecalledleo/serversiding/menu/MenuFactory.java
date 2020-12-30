package adudecalledleo.serversiding.menu;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class MenuFactory implements NamedScreenHandlerFactory {
    private final Text displayName;
    private final Supplier<MenuHandler> handlerFactory;

    public MenuFactory(Text displayName, Supplier<MenuHandler> handlerFactory) {
        this.displayName = displayName;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public Text getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new MenuScreenHandler(syncId, handlerFactory.get(), player);
    }
}
