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
        void rotate(float angle, boolean degrees);
    }

    interface Toggleable {
        void enable();
        void disable();
        boolean isEnabled();

        default void setEnabled(boolean enabled) {
            boolean wasEnabled = isEnabled();
            if (enabled && !wasEnabled)
                enable();
            else if (!enabled && wasEnabled)
                disable();
        }
    }

    interface ScissorManager extends Toggleable {
        void setRect(int x, int y, int w, int h);
    }

    interface BlendManager extends Toggleable {
        void setFunction(int srcFactor, int dstFactor);
        void setFunctionSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha);
        void setDefaultFunction();
    }

    interface AlphaTestManager extends Toggleable {
        void setFunction(int function, float reference);
        void setDefaultFunction();
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

    boolean isSmoothShading();
    void setSmoothShading(boolean smoothShading);

    @NotNull MatrixStack matrixStack();
    @NotNull ScissorManager scissorManager();
    @NotNull BlendManager blendManager();
    @NotNull AlphaTestManager alphaTestManager();
    @NotNull TextureManager textureManager();
    @NotNull TextRenderer textRenderer();

    @NotNull VertexConsumer begin(int drawMode);

    default void fill(float x, float y, float w, float h, int color) {
        int r = ColorUtil.unpackRed(color);
        int g = ColorUtil.unpackGreen(color);
        int b = ColorUtil.unpackBlue(color);
        int a = ColorUtil.unpackAlpha(color);

        VertexConsumer consumer = begin(GL11.GL_QUADS);
        VertexBuilder.buildQuad(consumer, VertexBuilder.color(r, g, b, a), x, y, w, h).end();
    }

    // TODO This doesn't render. Why?
    default void fillGradient(float x, float y, float w, float h, int color1, int color2) {
        boolean smoothShadingEnabled = isSmoothShading();
        boolean blendEnabled = blendManager().isEnabled();
        boolean alphaTestEnabled = alphaTestManager().isEnabled();
        boolean textureEnabled = textureManager().isEnabled();
        setSmoothShading(true);
        blendManager().enable();
        blendManager().setDefaultFunction();
        alphaTestManager().disable();
        textureManager().disable();

        int r1 = ColorUtil.unpackRed(color1);
        int g1 = ColorUtil.unpackGreen(color1);
        int b1 = ColorUtil.unpackBlue(color1);
        int a1 = ColorUtil.unpackAlpha(color1);

        int r2 = ColorUtil.unpackRed(color2);
        int g2 = ColorUtil.unpackGreen(color2);
        int b2 = ColorUtil.unpackBlue(color2);
        int a2 = ColorUtil.unpackAlpha(color2);

        VertexConsumer consumer = begin(GL11.GL_QUADS);
        VertexBuilder.buildQuad(consumer, (consumer1, id, x1, y1) -> {
            if (id < 2)
                consumer1.color(r1, g1, b1, a1);
            else
                consumer1.color(r2, g2, b2, a2);
        }, x, y, w, h).end();

        textureManager().setEnabled(textureEnabled);
        alphaTestManager().setEnabled(alphaTestEnabled);
        blendManager().setEnabled(blendEnabled);
        setSmoothShading(smoothShadingEnabled);
    }

    default void drawTexture(Identifier id, float x, float y, int w, int h) {
        boolean textureManagerEnabled = textureManager().isEnabled();
        textureManager().enable();

        textureManager().bind(id);
        VertexConsumer consumer = begin(GL11.GL_QUADS);
        VertexBuilder.buildQuad(consumer, VertexBuilder.textureQuad(0, 0, w, h, w, h), x, y, w, h).end();

        textureManager().setEnabled(textureManagerEnabled);
    }
}
