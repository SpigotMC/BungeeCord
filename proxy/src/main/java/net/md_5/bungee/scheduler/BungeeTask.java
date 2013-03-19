package net.md_5.bungee.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

@Data
public class BungeeTask implements ScheduledTask
{

    private final int id;
    private final Plugin owner;
    private final Runnable task;
    @Setter(AccessLevel.NONE)
    private ScheduledFuture<?> future;

    @Override
    public long getDelay(TimeUnit unit)
    {
        return future.getDelay( unit );
    }

    BungeeTask setFuture(ScheduledFuture<?> future)
    {
        this.future = future;
        return this;
    }
}
