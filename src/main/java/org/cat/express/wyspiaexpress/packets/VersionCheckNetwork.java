package org.cat.express.wyspiaexpress.packets;
import net.minecraft.util.Identifier;
import org.cat.express.wyspiaexpress.WyspiaExpress;

public final class VersionCheckNetwork {
    public static final Identifier VERSION_QUERY_ID =
             Identifier.of(WyspiaExpress.MOD_ID, "version_check");

    public static final Identifier VERSION_QUERY_FORCE_CRAWL_ID =
            Identifier.of(WyspiaExpress.MOD_ID, "version_check_force_crawl");
}


