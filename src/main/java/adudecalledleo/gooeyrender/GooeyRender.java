package adudecalledleo.gooeyrender;

import adudecalledleo.lionutils.LoggerUtil;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Logger;

public class GooeyRender implements ModInitializer {
    public static final String MOD_ID = "gooeyrender";
    public static final String MOD_NAME = "GooeyRender";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("GooeyRender is ready to draw stuff!");
    }
}
