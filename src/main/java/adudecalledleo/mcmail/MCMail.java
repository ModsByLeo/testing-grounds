package adudecalledleo.mcmail;

import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.mcmail.impl.MailboxProviderImpl;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class MCMail implements ModInitializer {
    public static final UUID NIL_UUID = new UUID(0, 0);

    public static final String MOD_ID = "mcmail";
    public static final String MOD_NAME = "MCMail";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        MailboxProviderImpl.initialize();
        LOGGER.info("Postal service ready!");
    }
}
