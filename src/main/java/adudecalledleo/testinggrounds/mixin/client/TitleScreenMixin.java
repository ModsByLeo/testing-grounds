package adudecalledleo.testinggrounds.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private TitleScreenMixin() {
        super(null);
        throw new IllegalStateException("Mixin constructor invoked");
    }

    private static final @Unique boolean EARTHQUAKE_MODE =
            Boolean.parseBoolean(System.getProperty("adudecalledleo.testinggrounds.earthquake_mode", "false"));

    private static final @Unique MutableText DISCLAIMER =
            new LiteralText("Please uninstall Testing Grounds.")
                    .styled(style -> style.withFormatting(Formatting.BOLD, Formatting.UNDERLINE));

    @Inject(method = "render", at = @At("HEAD"))
    public void dropLikeAnEarthquake(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (EARTHQUAKE_MODE) {
            matrices.push();
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            matrices.translate(rnd.nextInt(10) - 5, rnd.nextInt(10) - 5, 0);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rnd.nextFloat() * 10 - 5));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderNotice(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (EARTHQUAKE_MODE)
            matrices.pop();
        matrices.push();
        matrices.translate(width / 2f, 4, 0);
        matrices.scale(2, 2, 1);
        float f = Util.getMeasuringTimeMs() / 500f;
        float sat = MathHelper.sin(f) * .8f;
        int col = MathHelper.hsvToRgb(0, sat, 1);
        drawCenteredText(matrices, textRenderer, DISCLAIMER, 0, 0, col);
        matrices.pop();
    }
}
