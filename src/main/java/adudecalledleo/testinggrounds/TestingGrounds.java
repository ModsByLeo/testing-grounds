package adudecalledleo.testinggrounds;

import adudecalledleo.lionutils.LoggerUtil;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Logger;

public class TestingGrounds implements ModInitializer {
    public static final String MOD_ID = "testinggrounds";
    public static final String MOD_NAME = "Testing Grounds";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
    }
}
