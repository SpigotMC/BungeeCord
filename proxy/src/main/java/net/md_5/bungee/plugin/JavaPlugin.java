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
    public void onHandshake(LoginEvent event)
    {
    }

    /**
     * Called after a user has been authed with minecraftt.net and is about to
     * log into the proxy.
     */
    public void onLogin(LoginEvent event)
    {
    }

    /**
     * Called when a user is connecting to a new server.
     */
    public void onServerConnect(ServerConnectEvent event)
    {
    }

    /**
     * Called when a plugin message is sent to the client or server
     */
    public void onPluginMessage(PluginMessageEvent event)
    {
    }

    /**
     * Called when a chat message is sent to the client or server
     */
    public void onChat(ChatEvent event)
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
