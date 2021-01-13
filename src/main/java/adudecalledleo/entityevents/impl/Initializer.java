package adudecalledleo.entityevents.impl;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Initializer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Entity Events");

    @Override
    public void onInitialize() {
        LOGGER.info("Entity Events initializing!!!");
        EntityDamageEventsInternals.initialize();
    }
}
