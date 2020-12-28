package adudecalledleo.gooeyrender;

import adudecalledleo.gooeyrender.api.GooeyRenderContext;
import adudecalledleo.lionutils.color.ColorConstants;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class RenderTest implements HudRenderCallback {
    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        render();
    }

    private float angle;

    private void render() {
        GooeyRenderContext ctx = GooeyRenderContext.get();
        ctx.textRenderer().draw(new LiteralText("Testing GooeyRender!"), 4, 4, ColorConstants.WHITE, true);
        try (GooeyRenderContext.Closeable ignored = ctx.matrixStack().push()) {
            ctx.matrixStack().translate(24, 32);
            ctx.matrixStack().rotate(angle, true);
            angle += 1 + ctx.tickDelta();
            angle %= 360f;
            ctx.fillGradient(-16, -16, 32, 32, ColorConstants.RED, ColorConstants.GREEN);
            ctx.fill(-8, -8, 16, 16, ColorConstants.BLUE);
        }
    }
}
