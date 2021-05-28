package net.md_5.bungee.api.plugin;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;

public abstract class PluginManager
{
    PluginManager()
    {
    }

    /**
     * Register a command so that it may be executed.
     *
     * @param plugin the plugin owning this command
     * @param command the command to register
     */
    public abstract void registerCommand(Plugin plugin, Command command);

    /**
     * Unregister a command so it will no longer be executed.
     *
     * @param command the command to unregister
     */
    public abstract void unregisterCommand(Command command);

    /**
     * Unregister all commands owned by a {@link net.md_5.bungee.api.plugin.Plugin}
     *
     * @param plugin the plugin to register the commands of
     */
    public abstract void unregisterCommands(Plugin plugin);

    /**
     * Checks if the command is registered and can possibly be executed by the
     * sender (without taking permissions into account).
     *
     * @param commandName the name of the command
     * @param sender the sender executing the command
     * @return whether the command will be handled
     */
    public abstract boolean isExecutableCommand(String commandName, CommandSender sender);

    public abstract boolean dispatchCommand(CommandSender sender, String commandLine);

    /**
     * Execute a command if it is registered, else return false.
     *
     * @param sender the sender executing the command
     * @param commandLine the complete command line including command name and
     * arguments
     * @param tabResults list to place tab results into. If this list is non
     * null then the command will not be executed and tab results will be
     * returned instead.
     * @return whether the command was handled
     */
    public abstract boolean dispatchCommand(CommandSender sender, String commandLine, List<String> tabResults);

    /**
     * Returns the {@link net.md_5.bungee.api.plugin.Plugin} objects corresponding to all loaded plugins.
     *
     * @return the set of loaded plugins
     */
    public abstract Collection<Plugin> getPlugins();

    /**
     * Returns a loaded plugin identified by the specified name.
     *
     * @param name of the plugin to retrieve
     * @return the retrieved plugin or null if not loaded
     */
    public abstract Plugin getPlugin(String name);

    public abstract void loadPlugins();

    public abstract void enablePlugins();

    /**
     * Load all plugins from the specified folder.
     *
     * @param folder the folder to search for plugins in
     */
    public abstract void detectPlugins(File folder);

    /**
     * Dispatch an event to all subscribed listeners and return the event once
     * it has been handled by these listeners.
     *
     * @param <T> the type bounds, must be a class which extends event
     * @param event the event to call
     * @return the called event
     */
    public abstract <T extends Event> T callEvent(T event);

    /**
     * Register a {@link net.md_5.bungee.api.plugin.Listener} for receiving called events. Methods in this
     * Object which wish to receive events must be annotated with the
     * {@link net.md_5.bungee.event.EventHandler} annotation.
     *
     * @param plugin the owning plugin
     * @param listener the listener to register events for
     */
    public abstract void registerListener(Plugin plugin, Listener listener);

    /**
     * Unregister a {@link net.md_5.bungee.api.plugin.Listener} so that the events do not reach it anymore.
     *
     * @param listener the listener to unregister
     */
    public abstract void unregisterListener(Listener listener);

    /**
     * Unregister all of a Plugin's listener.
     *
     * @param plugin target plugin
     */
    public abstract void unregisterListeners(Plugin plugin);

    /**
     * Get an unmodifiable collection of all registered commands.
     *
     * @return commands
     */
    public abstract Collection<Map.Entry<String, Command>> getCommands();

    /**
     * Checks if {@code plugin} transitively depends on {@code depend}
     * @param plugin the plugin to start looking for dependencies
     * @param depend the depneding plugin to find
     * @return if {@code plugin} transitively depends on {@code depend}
     */
    public abstract boolean isTransitiveDepend(PluginDescription plugin, PluginDescription depend);
}
