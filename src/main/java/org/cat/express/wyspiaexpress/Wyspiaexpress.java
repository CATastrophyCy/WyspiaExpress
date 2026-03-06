package org.cat.express.wyspiaexpress;


import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cat.express.wyspiaexpress.config.WyspiaExpressServerConfig;

public class Wyspiaexpress implements ModInitializer {
    public static final String MOD_ID = "wyspiaexpress";
    // Create the global logger
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final WyspiaExpressServerConfig CONFIG = WyspiaExpressServerConfig.createAndLoad();

    @Override
    public void onInitialize() {
        LOGGER.info("WyspiaExpress is initializing...");
        WyspiaExpressRoles.init();

    }
}
