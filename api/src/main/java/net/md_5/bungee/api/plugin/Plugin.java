package net.md_5.bungee.api.plugin;

import lombok.Getter;

/**
 * Represents any Plugin that may be loaded at runtime to enhance existing
 * functionality.
 */
public class Plugin
{

    @Getter
    private PluginDescription description;

    /**
     * Called when this plugin is enabled.
     */
    public void onEnable()
    {
    }

    /**
     * Called when this plugin is disabled.
     */
    public void onDisable()
    {
    }

    /**
     * Called by the loader to initialize the fields in this plugin.
     *
     * @param description the description that describes this plugin
     */
    final void init(PluginDescription description)
    {
        this.description = description;
    }
}
