package adudecalledleo.mcmail;

import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.mcmail.impl.MailboxProviderImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.Logger;

public class MCMail implements ModInitializer {
    public static final String MOD_ID = "mcmail";
    public static final String MOD_NAME = "MCMail";

    public static final Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(MailboxProviderImpl::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(MailboxProviderImpl::onServerStopped);
    }
}
