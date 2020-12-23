package adudecalledleo.gooeyrender.impl;

import adudecalledleo.gooeyrender.api.GooeyRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.List;

@ApiStatus.Internal
public final class GooeyRenderContextImpl implements GooeyRenderContext {
    private static GooeyRenderContextImpl instance;

    public static GooeyRenderContextImpl getInstance() {
        if (instance == null)
            instance = new GooeyRenderContextImpl();
        return instance;
    }

    private static final class MatrixStackImpl implements MatrixStack {
        private static final Vector3f Z_ONLY = new Vector3f(0, 0, 1);

        private final net.minecraft.client.util.math.MatrixStack delegate;

        public MatrixStackImpl() {
            delegate = new net.minecraft.client.util.math.MatrixStack();
        }

        @Override
        public void push() {
            delegate.push();
        }

        @Override
        public void pop() {
            delegate.pop();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public void translate(float x, float y) {
            delegate.translate(x, y, 0);
        }

        @Override
        public void scale(float x, float y) {
            delegate.scale(x, y, 1);
        }

        @Override
        public void rotate(float angle, boolean degrees) {
            delegate.multiply(new Quaternion(Z_ONLY, angle, degrees));
        }
    }

    private static abstract class ToggleableImpl implements Toggleable {
        protected boolean enabled;

        public ToggleableImpl() {
            disable();
        }

        @Override
        public void enable() {
            if (!enabled) {
                enabled = true;
                onEnabled();
            }
        }

        @Override
        public void disable() {
            if (enabled) {
                enabled = false;
                onDisabled();
            }
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        protected abstract void onEnabled();
        protected abstract void onDisabled();
    }

    private static final class ScissorManagerImpl extends ToggleableImpl implements ScissorManager {
        @Override
        public void setRect(int x, int y, int w, int h) {
            Window window = MinecraftClient.getInstance().getWindow();

            // grab some values for scaling
            int windowHeight = window.getHeight();
            double scale = window.getScaleFactor();
            int scaledWidth = (int) (w * scale);
            int scaledHeight = (int) (h * scale);

            // send our rect to GL! this scales the coordinates and corrects the Y value,
            // since GL expects the bottom-left point of the rect and *also* expects the bottom of the screen to be 0 here
            // expression for Y coordinate adapted from vini2003's Spinnery (code snippet released under WTFPL)
            GL11.glScissor((int) (x * scale), (int) (windowHeight - (y * scale) - scaledHeight), scaledWidth, scaledHeight);
        }

        @Override
        protected void onEnabled() {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        protected void onDisabled() {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    private static final class TextureManagerImpl extends ToggleableImpl implements TextureManager {
        @Override
        public void bind(Identifier id) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(id);
        }

        @Override
        protected void onEnabled() {
            RenderSystem.enableTexture();
        }

        @Override
        protected void onDisabled() {
            RenderSystem.disableTexture();
        }
    }

    private static final class BlendManagerImpl extends ToggleableImpl implements BlendManager {
        @Override
        public void setFunction(int srcFactor, int dstFactor) {
            RenderSystem.blendFunc(srcFactor, dstFactor);
        }

        @Override
        public void setFunctionSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
            RenderSystem.blendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
        }

        @Override
        public void setDefaultFunction() {
            RenderSystem.defaultBlendFunc();
        }

        @Override
        protected void onEnabled() {
            RenderSystem.enableBlend();
        }

        @Override
        protected void onDisabled() {
            RenderSystem.disableBlend();
        }
    }

    private final class AlphaTestManagerImpl extends ToggleableImpl implements AlphaTestManager {
        @Override
        public void setFunction(int function, float reference) {
            //noinspection deprecation
            RenderSystem.alphaFunc(function, reference);
        }

        @Override
        public void setDefaultFunction() {
            RenderSystem.defaultAlphaFunc();
        }

        @Override
        protected void onEnabled() {
            //noinspection deprecation
            RenderSystem.enableAlphaTest();
        }

        @Override
        protected void onDisabled() {
            //noinspection deprecation
            RenderSystem.disableAlphaTest();
        }
    }

    private final class TextRendererImpl implements TextRenderer {
        private final net.minecraft.client.font.TextRenderer delegate;

        public TextRendererImpl() {
            delegate = MinecraftClient.getInstance().textRenderer;
        }

        @Override
        public int lineHeight() {
            return delegate.fontHeight;
        }

        @Override
        public int width(Text text) {
            return delegate.getWidth(text);
        }

        private void draw0(OrderedText text, int x, int y, int color, boolean shadow) {
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            delegate.draw(text, x, y, color, shadow,
                    matrixStack.delegate.peek().getModel(), immediate, false, 0, 0xF000F0);
            immediate.draw();
        }

        @Override
        public void draw(Text text, int x, int y, int color, boolean shadow) {
            draw0(text.asOrderedText(), x, y, color, shadow);
        }

        @Override
        public int wrappedHeight(Text text, int maxWidth) {
            return delegate.getStringBoundedHeight(text.getString(), maxWidth);
        }

        @Override
        public void drawWrapped(Text text, int x, int y, int maxWidth, int color, boolean shadow) {
            List<OrderedText> lines = delegate.wrapLines(text, maxWidth);
            for (OrderedText line : lines) {
                draw0(line, x, y, color, shadow);
                y += delegate.fontHeight + 1;
            }
        }
    }

    private final class VertexConsumerImpl implements VertexConsumer {
        private final Tessellator tessellator;
        private final BufferBuilder delegate;

        public VertexConsumerImpl(int drawMode) {
            tessellator = Tessellator.getInstance();
            delegate = Tessellator.getInstance().getBuffer();
            delegate.begin(drawMode, textureManager.enabled ? VertexFormats.POSITION_COLOR_TEXTURE : VertexFormats.POSITION_COLOR);
        }

        @Override
        public @NotNull VertexConsumer vertex(float x, float y) {
            delegate.vertex(matrixStack.delegate.peek().getModel(), x, y, 0);
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(r, g, b, a);
            return this;
        }

        @Override
        public @NotNull VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public void next() {
            delegate.next();
        }

        @Override
        public void end() {
            tessellator.draw();
        }
    }

    private final MatrixStackImpl matrixStack;
    private final ScissorManagerImpl scissorManager;
    private final BlendManagerImpl blendManager;
    private final AlphaTestManagerImpl alphaTestManager;
    private final TextureManagerImpl textureManager;
    private final TextRendererImpl textRenderer;

    private boolean isReady;
    private float tickDelta;

    private boolean smoothShading;

    private GooeyRenderContextImpl() {
        matrixStack = new MatrixStackImpl();
        scissorManager = new ScissorManagerImpl();
        blendManager = new BlendManagerImpl();
        alphaTestManager = new AlphaTestManagerImpl();
        textureManager = new TextureManagerImpl();
        textRenderer = new TextRendererImpl();
    }

    public void setup(float tickDelta) {
        this.tickDelta = tickDelta;
        isReady = true;

        scissorManager.disable();
        textureManager.disable();
    }

    public void invalidate() {
        isReady = false;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public float tickDelta() {
        return tickDelta;
    }

    @Override
    public boolean isSmoothShading() {
        return smoothShading;
    }

    @Override
    public void setSmoothShading(boolean smoothShading) {
        this.smoothShading = smoothShading;
        //noinspection deprecation
        RenderSystem.shadeModel(smoothShading ? GL11.GL_SMOOTH : GL11.GL_FLAT);
    }

    @Override
    public @NotNull MatrixStack matrixStack() {
        return matrixStack;
    }

    @Override
    public @NotNull ScissorManager scissorManager() {
        return scissorManager;
    }

    @Override
    public @NotNull BlendManager blendManager() {
        return blendManager;
    }

    @Override
    public @NotNull AlphaTestManager alphaTestManager() {
        return alphaTestManager;
    }

    @Override
    public @NotNull TextureManager textureManager() {
        return textureManager;
    }

    @Override
    public @NotNull TextRenderer textRenderer() {
        return textRenderer;
    }

    @Override
    public @NotNull VertexConsumer begin(int drawMode) {
        return new VertexConsumerImpl(drawMode);
    }
}
