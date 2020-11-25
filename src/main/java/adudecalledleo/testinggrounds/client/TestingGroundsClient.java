package adudecalledleo.testinggrounds.client;

import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.testinggrounds.TestingGrounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TestingGroundsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerUtil.getLogger(TestingGrounds.MOD_NAME + "|Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing client");
    }
}
