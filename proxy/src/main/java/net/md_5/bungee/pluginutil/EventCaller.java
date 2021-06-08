package net.md_5.bungee.pluginutil;

import java.lang.invoke.MethodHandles;

/**
 * Class to be loaded by {@linkplain net.md_5.bungee.api.plugin.EventCallerClassLoader EventCallerClassLoader}, may not
 * be referenced in other code directly.<br>
 * <br>
 * This class is string-referenced in EventCallerClassLoader; renaming or moving requires to update said string in said
 * class.<br>
 * The class is alone in this package to not be able to access other package-only
 * things.
 */
public final class EventCaller
{

    public static final MethodHandles.Lookup lookup = MethodHandles.lookup();
}
