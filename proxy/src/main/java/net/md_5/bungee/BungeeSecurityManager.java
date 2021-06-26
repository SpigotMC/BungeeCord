package net.md_5.bungee;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;

public class BungeeSecurityManager extends SecurityManager
{

    private static final boolean ENFORCE = false;
    private final Set<String> seen = new HashSet<>();

    private void checkRestricted(String text)
    {
        ClassLoader loader = getClassContext()[0].getClassLoader();

        // Bungee / system can do everything
        if ( loader == ClassLoader.getSystemClassLoader() || loader == null )
        {
            return;
        }

        AccessControlException ex = new AccessControlException( "Plugin violation: " + text );
        if ( ENFORCE )
        {
            throw ex;
        }

        StringWriter stack = new StringWriter();
        ex.printStackTrace( new PrintWriter( stack ) );
        if ( seen.add( stack.toString() ) )
        {
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
}
