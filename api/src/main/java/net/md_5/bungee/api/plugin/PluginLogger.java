package net.md_5.bungee.api.plugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import net.md_5.bungee.api.ProxyServer;

public class PluginLogger extends Logger
{

    private Logger bungeelogger;
    private String pluginName;

    protected PluginLogger(Plugin plugin)
    {
        super( plugin.getClass().getCanonicalName(), null );
        this.bungeelogger = Logger.getLogger("BungeeCord");
        pluginName = "[" + plugin.getDescription().getName() + "] ";
        setParent( ProxyServer.getInstance().getLogger() );
    }

    @Override
    public void log(LogRecord logRecord)
    {
        logRecord.setMessage( pluginName + logRecord.getMessage() );
        bungeelogger.log( logRecord );
    }
}
