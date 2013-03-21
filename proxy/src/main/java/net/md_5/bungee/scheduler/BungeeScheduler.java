package net.md_5.bungee.scheduler;

import com.google.common.base.Preconditions;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeScheduler implements TaskScheduler
{

    private final AtomicInteger taskCounter = new AtomicInteger();
    private final TIntObjectMap<BungeeTask> tasks = TCollections.synchronizedMap( new TIntObjectHashMap<BungeeTask>() );

    @Override
    public void cancel(int id)
    {
        BungeeTask task = tasks.remove( id );
        task.getFuture().cancel( false );
    }

    @Override
    public void cancel(ScheduledTask task)
    {
        cancel( task.getId() );
    }

    @Override
    public int cancel(Plugin plugin)
    {
        Set<ScheduledTask> toRemove = new HashSet<>();
        for ( ScheduledTask task : tasks.valueCollection() )
        {
            if ( task.getOwner() == plugin )
            {
                toRemove.add( task );
            }
        }
        for ( ScheduledTask task : toRemove )
        {
            cancel( task );
        }
        return toRemove.size();
    }

    @Override
    public ScheduledTask runAsync(Plugin owner, Runnable task)
    {
        return schedule( owner, task, 0, TimeUnit.MILLISECONDS );
    }

    @Override
    public ScheduledTask schedule(Plugin owner, Runnable task, long delay, TimeUnit unit)
    {
        return prepare( owner, task ).setFuture( BungeeCord.getInstance().executors.schedule( task, delay, unit ) );
    }

    @Override
    public ScheduledTask schedule(Plugin owner, Runnable task, long delay, long period, TimeUnit unit)
    {
        return prepare( owner, task ).setFuture( BungeeCord.getInstance().executors.scheduleWithFixedDelay( task, delay, period, unit ) );
    }

    private BungeeTask prepare(Plugin owner, Runnable task)
    {
        Preconditions.checkNotNull( owner, "owner" );
        Preconditions.checkNotNull( task, "task" );
        BungeeTask prepared = new BungeeTask( taskCounter.getAndIncrement(), owner, task );
        tasks.put( prepared.getId(), prepared );
        return prepared;
    }
}
