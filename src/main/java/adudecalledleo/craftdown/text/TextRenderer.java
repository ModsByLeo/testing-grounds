package adudecalledleo.craftdown.text;

import adudecalledleo.craftdown.node.Node;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TextRenderer {
    @NotNull List<Text> render(@NotNull Node root);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private Builder() { }

        public @NotNull TextRenderer build() {
            return new TextRendererImpl();
        }
    }
}
