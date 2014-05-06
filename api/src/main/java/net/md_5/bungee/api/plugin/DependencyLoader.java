package net.md_5.bungee.api.plugin;

import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that loads all plugins and their dependencies.
 */
@RequiredArgsConstructor
class DependencyLoader
{

    private final PluginManager pluginManager;

    /**
     * Unsorted map of all plugins.
     */
    private final Map<String, AvailablePluginWrapper> availablePlugins;

    /**
     * Map of all successfully loaded plugins.
     */
    @Getter
    private final Map<AvailablePluginWrapper, Plugin> plugins = new HashMap<>();

    /**
     * Previously failed plugin loads.
     */
    private final Map<AvailablePluginWrapper, Throwable> failedPlugins = new HashMap<>();

    private Logger getLogger()
    {
        ProxyServer proxyServer = ProxyServer.getInstance();
        // server is null in unit tests
        return proxyServer == null ? Logger.getGlobal() : proxyServer.getLogger();
    }

    /**
     * Compute the sorted plugin list.
     */
    public void loadPlugins()
    {
        for ( AvailablePluginWrapper plugin : availablePlugins.values() )
        {
            loadPlugin( plugin );
        }
    }

    /**
     * Load a plugin if not yet loaded. Errors are logged.
     */
    private void loadPlugin(AvailablePluginWrapper plugin)
    {
        try
        {
            loadPlugin0( plugin );
        } catch ( RepeatedDependencyException e )
        {
            getLogger().log( Level.WARNING, "Could not load " + plugin.getDescription().getName() + " because of previous error: " + e.getCause().getMessage() );
        } catch ( DependencyException e )
        {
            getLogger().log( Level.WARNING, "Could not load " + plugin.getDescription().getName() + ": " + e.getMessage() );
        }
    }

    /**
     * Attempt to load a plugin. Errors are thrown.
     * <p/>
     * <b>Package-local for unit testing, not meant to be accessed from other classes.</b>
     */
    void loadPlugin0(AvailablePluginWrapper plugin) throws DependencyException
    {
        Stack<AvailablePluginWrapper> dependencyStack = new Stack<>();
        loadPlugin( plugin, dependencyStack );
    }

    /**
     * Attempt to load a plugin. Errors are thrown.
     */
    private void loadPlugin(AvailablePluginWrapper plugin, Stack<AvailablePluginWrapper> dependencyStack) throws DependencyException
    {
        // already computed
        if ( plugins.containsKey( plugin ) ) return;
        // already failed & logged
        if ( failedPlugins.containsKey( plugin ) )
        {
            throw new RepeatedDependencyException( failedPlugins.get( plugin ) );
        }

        // circular dependency
        if ( dependencyStack.contains( plugin ) )
        {
            dependencyStack.push( plugin );
            StringBuilder path = new StringBuilder();
            for ( AvailablePluginWrapper dependency : dependencyStack )
            {
                if ( path.length() > 0 ) path.append( " -> " );
                path.append( dependency.getDescription().getName() );
            }
            throw new DependencyException( "Circular dependency detected: " + path );
        }

        for ( String dependencyName : Iterables.concat( plugin.getDescription().getDepends(), plugin.getDescription().getSoftDepends() ) )
        {
            boolean soft = !plugin.getDescription().getDepends().contains( dependencyName );
            AvailablePluginWrapper dependency = availablePlugins.get( dependencyName );
            if ( dependency == null )
            {
                if ( soft )
                {
                    // soft dependency, don't error
                    continue;
                }
                throw new DependencyException( "Missing dependency for " + plugin.getDescription().getName() + ": " + dependencyName );
            }

            dependencyStack.push( plugin );
            try
            {
                loadPlugin( dependency, dependencyStack );
            } catch ( DependencyException e )
            {
                failedPlugins.put( dependency, e );

                if ( soft )
                {
                    // soft dependency, don't error
                    continue;
                }

                throw e;
            } finally
            {
                dependencyStack.pop();
            }
        }

        try
        {
            Plugin impl = plugin.loadPlugin( pluginManager );

            impl.init( ProxyServer.getInstance(), plugin.getDescription() );
            plugins.put( plugin, impl );
            impl.onLoad();
            getLogger().log( Level.INFO, "Loaded plugin {0} version {1} by {2}", new Object[]
                    {
                            plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor()
                    } );
        } catch ( Throwable t )
        {
            failedPlugins.put( plugin, t );

            getLogger().log( Level.WARNING, "Error enabling plugin " + plugin.getDescription().getName(), t );
            throw new RepeatedDependencyException( t );
        }
    }

    static class DependencyException extends Exception
    {

        private DependencyException(String message)
        {
            super( message );
        }

        private DependencyException(Throwable cause)
        {
            super( cause );
        }
    }

    /**
     * DependencyException because an exception occured before, don't print too much info.
     */
    private static class RepeatedDependencyException extends DependencyException
    {

        private RepeatedDependencyException(@NonNull Throwable cause)
        {
            super( cause );
        }
    }
}
