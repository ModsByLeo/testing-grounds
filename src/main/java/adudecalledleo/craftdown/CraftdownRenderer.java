package adudecalledleo.craftdown;

import adudecalledleo.craftdown.impl.CraftdownRendererImpl;
import adudecalledleo.craftdown.node.Node;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CraftdownRenderer {
    @NotNull List<Text> render(@NotNull Node root);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private Builder() { }

        public @NotNull CraftdownRenderer build() {
            return new CraftdownRendererImpl();
        }
    }
}
