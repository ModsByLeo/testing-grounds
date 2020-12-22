package adudecalledleo.gooeyrender.api;

import adudecalledleo.gooeyrender.impl.GooeyRenderContextImpl;
import adudecalledleo.lionutils.color.ColorUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public interface GooeyRenderContext {
    static @NotNull GooeyRenderContext get() {
        return GooeyRenderContextImpl.getInstance();
    }

    interface MatrixStack {
        void push();
        void pop();
        boolean isEmpty();

        void translate(float x, float y);
        void scale(float x, float y);
        void rotate(float x, float y, float angle, boolean degrees);
    }

    interface Toggleable {
        void enable();
        void disable();
        boolean isEnabled();
    }

    interface ScissorManager extends Toggleable {
        void setRect(int x, int y, int w, int h);
    }

    interface TextureManager extends Toggleable {
        void bind(Identifier id);
    }

    interface TextRenderer {
        int lineHeight();
        int width(Text text);
        void draw(Text text, int x, int y, int color, boolean shadow);

        int wrappedHeight(Text text, int maxWidth);
        void drawWrapped(Text text, int x, int y, int maxWidth, int color, boolean shadow);
    }

    interface VertexConsumer {
        @NotNull VertexConsumer vertex(float x, float y);
        @NotNull VertexConsumer color(int r, int g, int b, int a);
        @NotNull VertexConsumer texture(float u, float v);

        default @NotNull VertexConsumer color(int r, int g, int b) {
            return color(r, g, b, 0xFF);
        }

        default @NotNull VertexConsumer color(float r, float g, float b, float a) {
            return color(MathHelper.floor(r * 255), MathHelper.floor(g * 255), MathHelper.floor(b * 255), MathHelper.floor(a * 255));
        }

        default @NotNull VertexConsumer color(float r, float g, float b) {
            return color(r, g, b, 1);
        }

        default @NotNull VertexConsumer color(int color) {
            return color(ColorUtil.unpackRed(color), ColorUtil.unpackGreen(color),
                    ColorUtil.unpackBlue(color), ColorUtil.unpackAlpha(color));
        }

        void next();
        void end();
    }

    boolean isReady();
    float tickDelta();

    @NotNull MatrixStack matrixStack();
    @NotNull ScissorManager scissorManager();
    @NotNull TextureManager textureManager();
    @NotNull TextRenderer textRenderer();

    @NotNull VertexConsumer begin(int drawMode);

    default void fill(int x, int y, int w, int h, int color) {
        int r = ColorUtil.unpackRed(color);
        int g = ColorUtil.unpackGreen(color);
        int b = ColorUtil.unpackBlue(color);
        int a = ColorUtil.unpackAlpha(color);

        VertexConsumer vc = begin(GL11.GL_QUADS);
        vc.vertex(x, y + h).color(r, g, b, a).next();
        vc.vertex(x + w, y + h).color(r, g, b, a).next();
        vc.vertex(x + w, y).color(r, g, b, a).next();
        vc.vertex(x, y).color(r, g, b, a).next();
        vc.end();
    }
}
