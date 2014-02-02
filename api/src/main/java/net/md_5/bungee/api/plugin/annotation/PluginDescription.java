package net.md_5.bungee.api.plugin.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDescription
{
    String name();
    String version();
    String author();
    String[] depends() default {};
    String description() default "";
}
