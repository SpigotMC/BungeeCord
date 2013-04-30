package net.md_5.bungee.scheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;

public class BungeeThreadPool extends ScheduledThreadPoolExecutor
{

    public BungeeThreadPool(ThreadFactory threadFactory)
    {
        super( Integer.MAX_VALUE, threadFactory );
        setKeepAliveTime( 5, TimeUnit.MINUTES );
        allowCoreThreadTimeOut( true );
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute( r, t );
        if ( t != null )
        {
            ProxyServer.getInstance().getLogger().log( Level.SEVERE, "Task caused exception whilst running", t );
        }
    }
}
