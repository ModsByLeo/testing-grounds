package adudecalledleo.globalclipboard;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Initializer implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Global Clipboard");

    @Override
    public void onInitializeClient() {
        System.setProperty("java.awt.headless", "false");
        ClipboardHolder.initialize();
        LOGGER.info("Global Clipboard initialized, because app-specific clipboards are cringe");
    }
}
