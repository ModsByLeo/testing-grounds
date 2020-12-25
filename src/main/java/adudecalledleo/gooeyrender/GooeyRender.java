package adudecalledleo.gooeyrender;

import adudecalledleo.lionutils.LoggerUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.apache.logging.log4j.Logger;

public class GooeyRender implements ClientModInitializer {
    public static final String MOD_ID = "gooeyrender";
    public static final String MOD_NAME = "GooeyRender";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new RenderTest());
        LOGGER.info("GooeyRender is ready to draw stuff!");
    }
}
