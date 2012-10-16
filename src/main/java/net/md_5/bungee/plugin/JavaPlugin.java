package net.md_5.bungee.plugin;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.command.Command;

/**
 * Base class which all proxy plugins should extend.
 */
public abstract class JavaPlugin
{

    /**
     * Description file.
     */
    PluginDescription description;

    /**
     * Called on enable.
     */
    public void onEnable()
    {
    }

    /**
     * Called on disable.
     */
    public void onDisable()
    {
    }

    /**
     * Called when a user connects with their name and address. To keep things
     * simple this name has not been checked with minecraft.net.
     */
    public void onHandshake(HandshakeEvent event)
    {
    }

    /**
     * Register a command for use with the proxy.
     */
    protected final void registerCommand(String label, Command command)
    {
        BungeeCord.instance.commandMap.put(label, command);
    }
}
