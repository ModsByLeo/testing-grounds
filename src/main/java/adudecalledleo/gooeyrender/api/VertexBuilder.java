package adudecalledleo.gooeyrender.api;

import org.jetbrains.annotations.NotNull;

public final class VertexBuilder {
    private VertexBuilder() { }

    interface VertexFunction {
        void apply(GooeyRenderContext.VertexConsumer consumer, int id, float x, float y);

        default @NotNull VertexFunction andThen(VertexFunction function) {
            return (consumer, id, x, y) -> {
                apply(consumer, id, x, y);
                function.apply(consumer, id, x, y);
            };
        }
    }

    public static @NotNull VertexFunction color(int r, int g, int b, int a) {
        return (consumer, id, x, y) -> consumer.color(r, g, b, a);
    }

    public static @NotNull VertexFunction textureQuad(int u, int v, int regionW, int regionH, int textureW, int textureH) {
        return (consumer, id, x, y) -> {
            switch (id) {
            case 0:
                consumer.texture(u / (float) textureW, (v + regionH) / (float) textureH);
                break;
            case 1:
                consumer.texture((u + regionW) / (float) textureW, (v + regionH) / (float) textureH);
                break;
            case 2:
                consumer.texture((u + regionW) / (float) textureW, v / (float) textureH);
                break;
            case 3:
                consumer.texture(u / (float) textureW, v / (float) textureH);
                break;
            default:
                break;
            }
        };
    }

    public static @NotNull VertexFunction textureQuadFull() {
        return (consumer, id, x, y) -> {
            switch (id) {
            case 0:
                consumer.texture(0, 1);
                break;
            case 1:
                consumer.texture(1, 1);
                break;
            case 2:
                consumer.texture(1, 0);
                break;
            case 3:
                consumer.texture(0, 0);
                break;
            default:
                break;
            }
        };
    }

    public static @NotNull GooeyRenderContext.VertexConsumer build(
            @NotNull GooeyRenderContext.VertexConsumer consumer,
            @NotNull VertexFunction function,
            float... vertices) {
        if (vertices.length == 0)
            return consumer;
        if (vertices.length % 2 != 0)
            throw new IllegalArgumentException("Vertices must be pairs of 2!");
        for (int i = 0; i < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];
            function.apply(consumer.vertex(x, y), i / 2, x, y);
            consumer.next();
        }
        return consumer;
    }

    public static @NotNull GooeyRenderContext.VertexConsumer buildQuad(
            @NotNull GooeyRenderContext.VertexConsumer consumer,
            @NotNull VertexFunction function,
            float x, float y, float w, float h) {
        float[] vertices = new float[8];
        vertices[0] = x + w; vertices[1] = y;
        vertices[2] = x; vertices[3] = y;
        vertices[4] = x; vertices[5] = y + h;
        vertices[6] = x + w; vertices[7] = y + h;
        return build(consumer, function, vertices);
    }
}
