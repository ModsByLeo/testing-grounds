package adudecalledleo.gooeyrender.mixin;

import adudecalledleo.gooeyrender.impl.GooeyRenderContextImpl;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/DiffuseLighting;enableGuiDepthLighting()V"))
    public void setupContext(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        GooeyRenderContextImpl.getInstance().setup(tickDelta);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void invalidateContext(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        GooeyRenderContextImpl.getInstance().invalidate();
    }
}