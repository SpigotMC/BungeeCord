package net.md_5.bungee.api.plugin;

import java.util.logging.Logger;

public class PluginLogger extends Logger
{
    protected PluginLogger(Plugin plugin)
    {
        super( plugin.getDescription().getName(), null );
        setParent( plugin.getProxy().getLogger() );
    }
}
