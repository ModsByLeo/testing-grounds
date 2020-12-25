package adudecalledleo.serversiding;

import adudecalledleo.lionutils.LoggerUtil;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;

public class ServerSiding implements ModInitializer {
    public static final String MOD_ID = "serversiding";
    public static final String MOD_NAME = "Server-Siding";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Server-Siding is ready for your plugin needs!");
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
