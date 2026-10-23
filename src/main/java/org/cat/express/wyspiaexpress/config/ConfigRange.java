package org.cat.express.wyspiaexpress.config;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigRange {
    double min();
    double max();
}
