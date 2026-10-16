package org.cat.express.wyspiaexpress.util;

import net.fabricmc.loader.api.FabricLoader;
import org.cat.express.wyspiaexpress.WyspiaExpress;

/** Installed versions used by the client and server handshake. */
public final class VersionUtil {
    private VersionUtil() {}

    public static String getVersion() {
        return getModVersion(WyspiaExpress.MOD_ID);
    }

    public static String getForceCrawlVersion() {
        return getModVersion("forcecrawl");
    }

    public static String getModVersion(String modId) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("UNKNOWN");
    }
}
