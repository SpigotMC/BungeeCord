package net.md_5.bungee;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginClassloader;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;

public class BungeeSecurityManager extends SecurityManager
{

    private static final boolean ENFORCE = false;

    /**
     * @param stackDepth How far back (at least) we need to go in the call stack.
     */
    private ClassLoader getPluginContextClassLoader(int stackDepth)
    {
        Class[] context = getClassContext();
        for ( int i = stackDepth + 1; i < context.length; i++ )
        {
            ClassLoader loader = context[i].getClassLoader();

            // Bungee can do everything
            if ( loader == ClassLoader.getSystemClassLoader() )
            {
                break;
            }

            // Allow external packages from the system class loader to create threads.
            if ( loader == null )
            {
                if ( !context[i].getName().startsWith( "java" ) )
                {
                    break;
                }
            }

            // Everyone but system can't do anything
            if ( loader != null )
            {
                return loader;
            }
        }
        return null;
    }

    private void checkRestricted(String text)
    {
        ClassLoader loader = getPluginContextClassLoader(2);

        // Everyone but system can't do anything
        if ( loader != null )
        {
            AccessControlException ex = new AccessControlException( "Plugin violation: " + text );
            if ( ENFORCE )
            {
                throw ex;
            }

            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Plugin performed restricted action, please inform them to use proper API methods: " + text, ex );
        }
    }

    @Override
    public void checkExit(int status)
    {
        checkRestricted( "Exit: Cannot close VM" );
    }

    @Override
    public void checkAccess(ThreadGroup g)
    {
        if ( !( g instanceof GroupedThreadFactory.BungeeGroup ) )
        {
            checkRestricted( "Illegal thread group access" );
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context)
    {
        checkPermission( perm );
    }

    @Override
    public void checkPermission(Permission perm)
    {
        switch ( perm.getName() )
        {
            case "setSecurityManager":
                throw new AccessControlException( "Restricted Action", perm );
        }
    }

    @Override
    public ThreadGroup getThreadGroup() {
        ClassLoader loader = getPluginContextClassLoader(1);
        if ( loader instanceof PluginClassloader )
        {
            Plugin plugin = ( (PluginClassloader) loader ).getPlugin();
            if ( plugin != null )
            {
                return plugin.getThreadGroup();
            }
        }
        return super.getThreadGroup();
    }
}
