package org.cat.express.wyspiaexpress.config;

import java.util.List;
import org.cat.express.wyspiaexpress.shop.EnumShopEntry;

@ConfigModel("ConfigAnnotationTest")
public class ConfigAnnotationTestModel {
    public int count = 3;
    public List<EnumShopEntry> items = List.of(EnumShopEntry.KNIFE);
    public Nested nested = new Nested();
    public static class Nested {
        public boolean enabled = true;
    }
}
