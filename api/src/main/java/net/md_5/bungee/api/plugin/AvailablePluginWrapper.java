package net.md_5.bungee.api.plugin;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper class for an available plugin. Plugin description (dependencies etc) is known but the actual plugin has not been loaded yet.
 */
@RequiredArgsConstructor
@Getter
abstract class AvailablePluginWrapper
{

    /**
     * The plugin description
     */
    @NonNull
    private final PluginDescription description;

    /**
     * Attempt to load this plugin. Should not call any onLoad or onEnable methods.
     *
     * @throws java.lang.Exception if something goes wrong.
     */
    @NonNull
    public abstract Plugin loadPlugin(PluginManager manager) throws Exception;
}
