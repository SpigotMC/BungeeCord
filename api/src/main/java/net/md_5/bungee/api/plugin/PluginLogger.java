package net.md_5.bungee.api.plugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;

public class PluginLogger extends Logger
{

    private String pluginName;

    protected PluginLogger(Plugin plugin)
    {
        super( plugin.getClass().getCanonicalName(), null );
        pluginName = "[" + plugin.getDescription().getName() + "] ";
    }

    @Override
    public void log(LogRecord logRecord)
    {
        logRecord.setMessage( pluginName + logRecord.getMessage() );
        ProxyServer.getInstance().getLogger().log( logRecord );
    }
}
