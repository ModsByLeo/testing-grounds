package adudecalledleo.serversiding.impl.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Accessor DefaultedList<ItemStack> getTrackedStacks();
}
