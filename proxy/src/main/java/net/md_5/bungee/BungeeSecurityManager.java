package net.md_5.bungee;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;

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
                if ( !context[i].getName().startsWith( "java.lang" ) )
                {
                    break;
                }
            }

            // Everyone but system can't do anything
            if ( loader != null )
            {
                System.out.println( loader );
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
    public ThreadGroup getThreadGroup()
    {
        checkRestricted( "Thread Creation: Use scheduler" );
        return super.getThreadGroup();
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
