package net.md_5.bungee.api.scheduler;

import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task scheduled for execution by the {@link TaskScheduler}.
 */
public interface ScheduledTask
{

    /**
     * Gets the unique ID of this task.
     *
     * @return this tasks ID
     */
    int getId();

    /**
     * Return the plugin which scheduled this task for execution.
     *
     * @return the owning plugin
     */
    @NotNull
    Plugin getOwner();

    /**
     * Get the actual method which will be executed by this task.
     *
     * @return the {@link Runnable} behind this task
     */
    @NotNull
    Runnable getTask();

    /**
     * Cancel this task to suppress subsequent executions.
     */
    void cancel();
}
