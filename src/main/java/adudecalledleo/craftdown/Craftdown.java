package adudecalledleo.craftdown;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Craftdown implements ModInitializer {
    public static final String MOD_ID = "craftdown";
    public static final String MOD_NAME = "Craftdown";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Ready to mark down!");
    }
}
