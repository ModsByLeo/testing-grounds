package adudecalledleo.serversiding.menu.simple.button;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@FunctionalInterface
public interface Label {
    static @NotNull Label of(@NotNull Item icon, @NotNull Text title, @NotNull Text... description) {
        return () -> ItemStackBuilder.create()
                .setItem(icon)
                .setCustomName(title.shallowCopy()
                        .styled(style -> style.withColor(Formatting.BLUE).withBold(true).withItalic(false))
                )
                .addLore(Stream.of(description)
                        .map(Text::shallowCopy)
                        .map(line -> line.styled(style -> style.withColor(Formatting.GRAY).withItalic(true)))
                        .collect(Collectors.toList()))
                .build();
    }

    @NotNull ItemStack getStack();
}
