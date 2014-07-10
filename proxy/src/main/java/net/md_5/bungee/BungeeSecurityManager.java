package net.md_5.bungee;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;

public class BungeeSecurityManager extends SecurityManager
{

    private static final boolean ENFORCE = false;

    private void checkRestricted(String text)
    {
        Class[] context = getClassContext();
        for ( int i = 2; i < context.length; i++ )
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
                AccessControlException ex = new AccessControlException( "Plugin violation: " + text );
                if ( ENFORCE )
                {
                    throw ex;
                }

                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Plugin performed restricted action, please inform them to use proper API methods: " + text, ex );
                break;
            }
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
}
