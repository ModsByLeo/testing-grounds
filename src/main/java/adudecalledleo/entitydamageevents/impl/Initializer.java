package adudecalledleo.entitydamageevents.impl;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Initializer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Entity Damage Events");

    @Override
    public void onInitialize() {
        LOGGER.info("Entity Damage Events initializing!!!");
        EntityDamageEventsInternals.initialize();
    }
}
