package net.md_5.bungee.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeScheduler implements TaskScheduler
{

    private final AtomicInteger taskCounter = new AtomicInteger();
    private final TIntObjectMap<BungeeTask> tasks = new TIntObjectHashMap<>();
    private final Multimap<Plugin, BungeeTask> tasksByPlugin = HashMultimap.create();
    private final Lock taskLock = new ReentrantLock();
    //
    private final Unsafe unsafe = new Unsafe()
    {

        @Override
        public ExecutorService getExecutorService(Plugin plugin)
        {
            return plugin.getExecutorService();
        }
    };

    @Override
    public void cancel(int id)
    {
        taskLock.lock();
        try
        {
            Preconditions.checkArgument( tasks.containsKey( id ), "task ID is invalid" );

            // End the task directly, so we don't end up in flames.
            BungeeTask task = tasks.remove( id );
            task.getRunning().set( false );
            tasksByPlugin.values().remove( task );
        } finally
        {
            taskLock.unlock();
        }
    }

    @Override
    public void cancel(ScheduledTask task)
    {
        task.cancel();
    }

    @Override
    public int cancel(Plugin plugin)
    {
        Set<ScheduledTask> toRemove = new HashSet<>();

        taskLock.lock();
        try
        {
            for ( ScheduledTask task : tasksByPlugin.get( plugin ) )
            {
                toRemove.add( task );
            }
        } finally
        {
            taskLock.unlock();
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
        return schedule( owner, task, delay, 0, unit );
    }

    @Override
    public ScheduledTask schedule(Plugin owner, Runnable task, long delay, long period, TimeUnit unit)
    {
        Preconditions.checkNotNull( owner, "owner" );
        Preconditions.checkNotNull( task, "task" );
        BungeeTask prepared = new BungeeTask( this, taskCounter.getAndIncrement(), owner, task, delay, period, unit );

        taskLock.lock();
        try
        {
            tasks.put( prepared.getId(), prepared );
            tasksByPlugin.put( owner, prepared );
        } finally
        {
            taskLock.unlock();
        }

        owner.getExecutorService().execute( prepared );
        return prepared;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }
}
