package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import org.yaml.snakeyaml.Yaml;

/**
 * Class to manage bridging between plugin duties and implementation duties, for
 * example event handling and plugin management.
 */
@RequiredArgsConstructor
public class PluginManager
{

    private static final Pattern argsSplit = Pattern.compile( " " );
    /*========================================================================*/
    private final ProxyServer proxy;
    /*========================================================================*/
    private final Yaml yaml = new Yaml();
    private final EventBus eventBus = new EventBus();
    private final Map<String, Plugin> plugins = new HashMap<>();
    private final Map<String, Command> commandMap = new HashMap<>();

    /**
     * Register a command so that it may be executed.
     *
     * @param plugin the plugin owning this command
     * @param command the command to register
     */
    public void registerCommand(Plugin plugin, Command command)
    {
        commandMap.put( command.getName().toLowerCase(), command );
        for ( String alias : command.getAliases() )
        {
            commandMap.put( alias.toLowerCase(), command );
        }
    }

    /**
     * Unregister a command so it will no longer be executed.
     *
     * @param command the command to unregister
     */
    public void unregisterCommand(Command command)
    {
        commandMap.values().remove( command );
    }

    /**
     * Execute a command if it is registered, else return false.
     *
     * @param sender the sender executing the command
     * @param commandLine the complete command line including command name and
     * arguments
     * @return whether the command was handled
     */
    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        String[] split = argsSplit.split( commandLine );
        Command command = commandMap.get( split[0].toLowerCase() );
        if ( command == null )
        {
            return false;
        }

        String permission = command.getPermission();
        if ( permission != null && !permission.isEmpty() && !sender.hasPermission( permission ) )
        {
            sender.sendMessage( ChatColor.RED + "You do not have permission to execute this command!" );
            return true;
        }

        String[] args = Arrays.copyOfRange( split, 1, split.length );
        try
        {
            command.execute( sender, args );
        } catch ( Exception ex )
        {
            sender.sendMessage( ChatColor.RED + "An internal error occurred whilst executing this command, please check the console log for details." );
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Error in dispatching command", ex );
        }
        return true;
    }

    /**
     * Returns the {@link Plugin} objects corresponding to all loaded plugins.
     *
     * @return the set of loaded plugins
     */
    public Collection<Plugin> getPlugins()
    {
        return plugins.values();
    }

    /**
     * Returns a loaded plugin identified by the specified name.
     *
     * @param name of the plugin to retrieve
     * @return the retrieved plugin or null if not loaded
     */
    public Plugin getPlugin(String name)
    {
        return plugins.get( name );
    }

    /**
     * Enable all plugins by calling the {@link Plugin#onEnable()} method.
     */
    public void enablePlugins()
    {
        Map<Plugin, Boolean> pluginStatuses = new HashMap<>();
        for ( Map.Entry<String, Plugin> entry : plugins.entrySet() )
        {
            Plugin plugin = entry.getValue();
            if ( !this.enablePlugin( pluginStatuses, new Stack<Plugin>(), plugin ) )
            {
                ProxyServer.getInstance().getLogger().warning( "Failed to enable " + entry.getKey() );
            }
        }
    }

    private boolean enablePlugin(Map<Plugin, Boolean> pluginStatuses, Stack<Plugin> dependStack, Plugin plugin)
    {
        if ( pluginStatuses.containsKey( plugin ) )
        {
            return pluginStatuses.get( plugin );
        }

        // success status
        boolean status = true;

        // try to load dependencies first
        for ( String dependName : plugin.getDescription().getDepends() )
        {
            Plugin depend = this.plugins.get( dependName );
            Boolean dependStatus = depend != null ? pluginStatuses.get( depend ) : Boolean.FALSE;

            if ( dependStatus == null )
            {
                if ( dependStack.contains( depend ) )
                {
                    StringBuilder dependencyGraph = new StringBuilder();
                    for ( Plugin element : dependStack )
                    {
                        dependencyGraph.append( element.getDescription().getName() ).append( " -> " );
                    }
                    dependencyGraph.append( plugin.getDescription().getName() ).append( " -> " ).append( dependName );
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Circular dependency detected: " + dependencyGraph );
                    status = false;
                } else
                {
                    dependStack.push( plugin );
                    dependStatus = this.enablePlugin( pluginStatuses, dependStack, depend );
                    dependStack.pop();
                }
            }

            if ( dependStatus == Boolean.FALSE )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} (required by {1}) is unavailable", new Object[]
                {
                    depend.getDescription().getName(), plugin.getDescription().getName()
                } );
                status = false;
            }

            if ( !status )
            {
                break;
            }
        }

        // do actual loading
        if ( status )
        {
            try
            {
                plugin.onEnable();
                ProxyServer.getInstance().getLogger().log( Level.INFO, "Enabled plugin {0} version {1} by {2}", new Object[]
                {
                    plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor()
                } );
            } catch ( Exception ex )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Exception encountered when loading plugin: " + plugin.getDescription().getName(), ex );
                status = false;
            }
        }

        pluginStatuses.put( plugin, status );
        return status;
    }

    /**
     * Load a plugin from the specified file. This file must be in jar format.
     * This will not enable plugins, {@link #enablePlugins()} must be called.
     *
     * @param file the file to load from
     * @throws Exception Any exceptions encountered when loading a plugin from
     * this file.
     */
    public void loadPlugin(File file) throws Exception
    {
        Preconditions.checkNotNull( file, "file" );
        Preconditions.checkArgument( file.isFile(), "Must load from file" );

        try ( JarFile jar = new JarFile( file ) )
        {
            JarEntry pdf = jar.getJarEntry( "plugin.yml" );
            Preconditions.checkNotNull( pdf, "Plugin must have a plugin.yml" );

            try ( InputStream in = jar.getInputStream( pdf ) )
            {
                PluginDescription desc = yaml.loadAs( in, PluginDescription.class );
                URLClassLoader loader = new PluginClassloader( new URL[]
                {
                    file.toURI().toURL()
                } );
                Class<?> main = loader.loadClass( desc.getMain() );
                Plugin plugin = (Plugin) main.getDeclaredConstructor().newInstance();

                plugin.init( proxy, desc, file );
                plugins.put( desc.getName(), plugin );
                plugin.onLoad();
                ProxyServer.getInstance().getLogger().log( Level.INFO, "Loaded plugin {0} version {1} by {2}", new Object[]
                {
                    desc.getName(), desc.getVersion(), desc.getAuthor()
                } );
            }
        }
    }

    /**
     * Load all plugins from the specified folder.
     *
     * @param folder the folder to search for plugins in
     */
    public void loadPlugins(File folder)
    {
        Preconditions.checkNotNull( folder, "folder" );
        Preconditions.checkArgument( folder.isDirectory(), "Must load from a directory" );

        File[] files = folder.listFiles();
        Arrays.sort( files );

        for ( File file : files )
        {
            if ( file.isFile() && file.getName().endsWith( ".jar" ) )
            {
                try
                {
                    loadPlugin( file );
                } catch ( Exception ex )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load plugin from file " + file, ex );
                }
            }
        }
    }

    /**
     * Dispatch an event to all subscribed listeners and return the event once
     * it has been handled by these listeners.
     *
     * @param <T> the type bounds, must be a class which extends event
     * @param event the event to call
     * @return the called event
     */
    public <T extends Event> T callEvent(T event)
    {
        Preconditions.checkNotNull( event, "event" );

        long start = System.nanoTime();
        eventBus.post( event );
        event.postCall();

        long elapsed = start - System.nanoTime();
        if ( elapsed > 250000 )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Event {0} took more {1}ns to process!", new Object[]
            {
                event, elapsed
            } );
        }
        return event;
    }

    /**
     * Register a {@link Listener} for receiving called events. Methods in this
     * Object which wish to receive events must be annotated with the
     * {@link Subscribe} annotation.
     *
     * @param plugin the owning plugin
     * @param listener the listener to register events for
     */
    public void registerListener(Plugin plugin, Listener listener)
    {
        eventBus.register( listener );
    }
}
