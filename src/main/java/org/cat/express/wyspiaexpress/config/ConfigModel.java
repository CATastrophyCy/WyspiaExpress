package org.cat.express.wyspiaexpress.config;

import java.lang.annotation.*;

/** Generates a typed, mode-aware view from ordinary fields, defaults and nested models. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ConfigModel {
    String value();
}
