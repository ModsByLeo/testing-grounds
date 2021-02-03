package adudecalledleo.entityevents.impl;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class Initializer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Entity Events");

    @Override
    public void onInitialize() {
        // DISABLED:preLaunch
        /*
        LOGGER.info("Entity Events initializing!!!");
        EntityDamageEventsInternals.initialize();
        EntityTickEventsInternals.initialize();*/
    }
}
