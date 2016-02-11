package net.md_5.bungee.api.plugin;

import net.md_5.bungee.api.CommandSender;

import java.util.Collection;
import java.util.List;

public interface PluginManager
{

    /**
     * Associates a command with a plugin. If the command already exists, the existing command is overridden with the new one.
     * @param plugin the plugin associated with this command
     * @param command the command to register
     */
    void registerCommand(Plugin plugin, Command command);

    /**
     * Unregisters a command.
     * @param command the command to unregister
     */
    void unregisterCommand(Command command);

    /**
     * Unregisters all of a plugin's commands.
     * @param plugin the plugin with commands to unregister
     */
    void unregisterCommands(Plugin plugin);

    /**
     * Dispatches a command to all registered commands. If an exception occurs during command execution, the exception will
     * be rethrown as an {@link CommandExecutionException}.
     * @param sender the sender to use
     * @param commandLine the full command to execute
     * @return whether or not the command was executed
     * @throws CommandExecutionException if an exception occurred whilst executing the command
     */
    boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandExecutionException;

    /**
     * Dispatches a command to all registered commands. If {@code tabResults} is non-null, asks for tab completion instead.
     * If an exception occurs during command execution, the exception will be rethrown as an {@link CommandExecutionException}.
     * @param sender the sender to use
     * @param commandLine the full command to execute
     * @param tabResults mutable {@code List} of results for tab complete, or {@code null} for command execution
     * @return whether or not the command was executed
     * @throws CommandExecutionException if an exception occurred whilst executing the command
     */
    boolean dispatchCommand(CommandSender sender, String commandLine, List<String> tabResults) throws CommandExecutionException;

    /**
     * Returns a collection of all plugins enabled on this proxy.
     * @return the plugins enabled
     */
    Collection<Plugin> getPlugins();

    /**
     * Returns a specific plugin with this {@code name}.
     * @param name the name to use
     * @return the plugin, if enabled
     */
    Plugin getPlugin(String name);

    /**
     * Posts an event to the BungeeCord event bus.
     * @param event the event to be fired
     * @param <T> the event type to use
     * @return the same event instance passed in
     */
    <T extends Event> T callEvent(T event);

    /**
     * Registers a listener associated with a plugin.
     * @param plugin the plugin associated with this listener
     * @param listener the listener to register
     */
    void registerListener(Plugin plugin, Listener listener);

    /**
     * Unregisters a listener.
     * @param listener the listener to deregister
     */
    void unregisterListener(Listener listener);

    /**
     * Unregisters all listeners associated with a plugin.
     * @param plugin the plugin whose listeners are to be unregistered
     */
    void unregisterListeners(Plugin plugin);
}
