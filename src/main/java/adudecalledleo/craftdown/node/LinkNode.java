package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public final class LinkNode extends Node {
    private final @NotNull URL url;

    public LinkNode(@NotNull URL url) {
        this.url = url;
    }

    public @NotNull URL getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "LinkNode{" +
                "url=" + url +
                '}';
    }
}
