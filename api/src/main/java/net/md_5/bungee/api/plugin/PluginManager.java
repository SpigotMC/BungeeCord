package net.md_5.bungee.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.event.EventBus;
import net.md_5.bungee.event.EventHandler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

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
    private final Yaml yaml;
    private final EventBus eventBus;
    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
    private final Map<String, Command> commandMap = new HashMap<>();
    private Map<String, PluginDescription> toLoad = new HashMap<>();
    private final Multimap<Plugin, Command> commandsByPlugin = ArrayListMultimap.create();
    private final Multimap<Plugin, Listener> listenersByPlugin = ArrayListMultimap.create();

    @SuppressWarnings("unchecked")
    public PluginManager(ProxyServer proxy)
    {
        this.proxy = proxy;

        // Ignore unknown entries in the plugin descriptions
        Constructor yamlConstructor = new Constructor();
        PropertyUtils propertyUtils = yamlConstructor.getPropertyUtils();
        propertyUtils.setSkipMissingProperties( true );
        yamlConstructor.setPropertyUtils( propertyUtils );
        yaml = new Yaml( yamlConstructor );

        eventBus = new EventBus( proxy.getLogger() );
    }

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
        commandsByPlugin.put( plugin, command );
    }

    /**
     * Unregister a command so it will no longer be executed.
     *
     * @param command the command to unregister
     */
    public void unregisterCommand(Command command)
    {
        while ( commandMap.values().remove( command ) );
        commandsByPlugin.values().remove( command );
    }

    /**
     * Unregister all commands owned by a {@link Plugin}
     *
     * @param plugin the plugin to register the commands of
     */
    public void unregisterCommands(Plugin plugin)
    {
        for ( Iterator<Command> it = commandsByPlugin.get( plugin ).iterator(); it.hasNext(); )
        {
            Command command = it.next();
            while ( commandMap.values().remove( command ) );
            it.remove();
        }
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        return dispatchCommand( sender, commandLine, null );
    }

    /**
     * Execute a command if it is registered, else return false.
     *
     * @param sender the sender executing the command
     * @param commandLine the complete command line including command name and
     * arguments
     * @return whether the command was handled
     */
    public boolean dispatchCommand(CommandSender sender, String commandLine, List<String> tabResults)
    {
        String[] split = argsSplit.split( commandLine, -1 );
        // Check for chat that only contains " "
        if ( split.length == 0 )
        {
            return false;
        }

        String commandName = split[0].toLowerCase();
        if ( sender instanceof ProxiedPlayer && proxy.getDisabledCommands().contains( commandName ) )
        {
            return false;
        }
        Command command = commandMap.get( commandName );
        if ( command == null )
        {
            return false;
        }

        String permission = command.getPermission();
        if ( permission != null && !permission.isEmpty() && !sender.hasPermission( permission ) )
        {
            if ( tabResults == null )
            {
                sender.sendMessage( proxy.getTranslation( "no_permission" ) );
            }
            return true;
        }

        String[] args = Arrays.copyOfRange( split, 1, split.length );
        try
        {
            if ( tabResults == null )
            {
                if ( proxy.getConfig().isLogCommands() )
                {
                    proxy.getLogger().log( Level.INFO, "{0} executed command: /{1}", new Object[]
                    {
                        sender.getName(), commandLine
                    } );
                }
                command.execute( sender, args );
            } else if ( commandLine.contains( " " ) && command instanceof TabExecutor )
            {
                for ( String s : ( (TabExecutor) command ).onTabComplete( sender, args ) )
                {
                    tabResults.add( s );
                }
            }
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

    public void loadPlugins()
    {
        Map<PluginDescription, Boolean> pluginStatuses = new HashMap<>();
        for ( Map.Entry<String, PluginDescription> entry : toLoad.entrySet() )
        {
            PluginDescription plugin = entry.getValue();
            if ( !enablePlugin( pluginStatuses, new Stack<PluginDescription>(), plugin ) )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Failed to enable {0}", entry.getKey() );
            }
        }
        toLoad.clear();
        toLoad = null;
    }

    public void enablePlugins()
    {
        for ( Plugin plugin : plugins.values() )
        {
            try
            {
                plugin.onEnable();
                ProxyServer.getInstance().getLogger().log( Level.INFO, "Enabled plugin {0} version {1} by {2}", new Object[]
                {
                    plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor()
                } );
            } catch ( Throwable t )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Exception encountered when loading plugin: " + plugin.getDescription().getName(), t );
            }
        }
    }

    private boolean enablePlugin(Map<PluginDescription, Boolean> pluginStatuses, Stack<PluginDescription> dependStack, PluginDescription plugin)
    {
        if ( pluginStatuses.containsKey( plugin ) )
        {
            return pluginStatuses.get( plugin );
        }

        // combine all dependencies for 'for loop'
        Set<String> dependencies = new HashSet<>();
        dependencies.addAll( plugin.getDepends() );
        dependencies.addAll( plugin.getSoftDepends() );

        // success status
        boolean status = true;

        // try to load dependencies first
        for ( String dependName : dependencies )
        {
            PluginDescription depend = toLoad.get( dependName );
            Boolean dependStatus = ( depend != null ) ? pluginStatuses.get( depend ) : Boolean.FALSE;

            if ( dependStatus == null )
            {
                if ( dependStack.contains( depend ) )
                {
                    StringBuilder dependencyGraph = new StringBuilder();
                    for ( PluginDescription element : dependStack )
                    {
                        dependencyGraph.append( element.getName() ).append( " -> " );
                    }
                    dependencyGraph.append( plugin.getName() ).append( " -> " ).append( dependName );
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Circular dependency detected: {0}", dependencyGraph );
                    status = false;
                } else
                {
                    dependStack.push( plugin );
                    dependStatus = this.enablePlugin( pluginStatuses, dependStack, depend );
                    dependStack.pop();
                }
            }

            if ( dependStatus == Boolean.FALSE && plugin.getDepends().contains( dependName ) ) // only fail if this wasn't a soft dependency
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "{0} (required by {1}) is unavailable", new Object[]
                {
                    String.valueOf( dependName ), plugin.getName()
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
                URLClassLoader loader = new PluginClassloader( new URL[]
                {
                    plugin.getFile().toURI().toURL()
                } );
                Class<?> main = loader.loadClass( plugin.getMain() );
                Plugin clazz = (Plugin) main.getDeclaredConstructor().newInstance();

                clazz.init( proxy, plugin );
                plugins.put( plugin.getName(), clazz );
                clazz.onLoad();
                ProxyServer.getInstance().getLogger().log( Level.INFO, "Loaded plugin {0} version {1} by {2}", new Object[]
                {
                    plugin.getName(), plugin.getVersion(), plugin.getAuthor()
                } );
            } catch ( Throwable t )
            {
                proxy.getLogger().log( Level.WARNING, "Error enabling plugin " + plugin.getName(), t );
            }
        }

        pluginStatuses.put( plugin, status );
        return status;
    }

    /**
     * Load all plugins from the specified folder.
     *
     * @param folder the folder to search for plugins in
     */
    public void detectPlugins(File folder)
    {
        Preconditions.checkNotNull( folder, "folder" );
        Preconditions.checkArgument( folder.isDirectory(), "Must load from a directory" );

        for ( File file : folder.listFiles() )
        {
            if ( file.isFile() && file.getName().endsWith( ".jar" ) )
            {
                try ( JarFile jar = new JarFile( file ) )
                {
                    JarEntry pdf = jar.getJarEntry( "bungee.yml" );
                    if ( pdf == null )
                    {
                        pdf = jar.getJarEntry( "plugin.yml" );
                    }
                    Preconditions.checkNotNull( pdf, "Plugin must have a plugin.yml or bungee.yml" );

                    try ( InputStream in = jar.getInputStream( pdf ) )
                    {
                        PluginDescription desc = yaml.loadAs( in, PluginDescription.class );
                        Preconditions.checkNotNull( desc.getName(), "Plugin from %s has no name", file );
                        Preconditions.checkNotNull( desc.getMain(), "Plugin from %s has no main", file );

                        desc.setFile( file );
                        toLoad.put( desc.getName(), desc );
                    }
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

        long elapsed = System.nanoTime() - start;
        if ( elapsed > 250000000 )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Event {0} took {1}ns to process!", new Object[]
            {
                event, elapsed
            } );
        }
        return event;
    }

    /**
     * Register a {@link Listener} for receiving called events. Methods in this
     * Object which wish to receive events must be annotated with the
     * {@link EventHandler} annotation.
     *
     * @param plugin the owning plugin
     * @param listener the listener to register events for
     */
    public void registerListener(Plugin plugin, Listener listener)
    {
        for ( Method method : listener.getClass().getDeclaredMethods() )
        {
            Preconditions.checkArgument( !method.isAnnotationPresent( Subscribe.class ),
                    "Listener %s has registered using deprecated subscribe annotation! Please update to @EventHandler.", listener );
        }
        eventBus.register( listener );
        listenersByPlugin.put( plugin, listener );
    }

    /**
     * Unregister a {@link Listener} so that the events do not reach it anymore.
     *
     * @param listener the listener to unregister
     */
    public void unregisterListener(Listener listener)
    {
        eventBus.unregister( listener );
        listenersByPlugin.values().remove( listener );
    }

    /**
     * Unregister all of a Plugin's listener.
     */
    public void unregisterListeners(Plugin plugin)
    {
        for ( Iterator<Listener> it = listenersByPlugin.get( plugin ).iterator(); it.hasNext(); )
        {
            eventBus.unregister( it.next() );
            it.remove();
        }
    }
}
