package net.md_5.bungee.api.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * This interface represents a scheduler which may be used to queue, delay and
 * execute tasks in an asynchronous fashion.
 */
public interface TaskScheduler
{

    /**
     * Cancel a task to prevent it from executing, or if its a repeating task,
     * prevent its further execution.
     *
     * @param id the id of the task to cancel
     */
    void cancel(int id);

    /**
     * Cancel a task to prevent it from executing, or if its a repeating task,
     * prevent its further execution.
     *
     * @param task the task to cancel
     */
    void cancel(ScheduledTask task);

    /**
     * Cancel all tasks owned by this plugin, this preventing them from being
     * executed hereon in.
     *
     * @param plugin the plugin owning the tasks to be cancelled
     * @return the number of tasks cancelled by this method
     */
    int cancel(Plugin plugin);

    /**
     * Schedule a task to be executed asynchronously. The task will commence
     * running as soon as this method returns.
     *
     * @param owner the plugin owning this task
     * @param task the task to run
     * @return the scheduled task
     */
    ScheduledTask runAsync(Plugin owner, Runnable task);

    /**
     * Schedules a task to be executed asynchronously after the specified delay
     * is up.
     *
     * @param owner the plugin owning this task
     * @param task the task to run
     * @param delay the delay before this task will be executed
     * @param unit the unit in which the delay will be measured
     * @return the scheduled task
     */
    ScheduledTask schedule(Plugin owner, Runnable task, long delay, TimeUnit unit);

    /**
     * Schedules a task to be executed asynchronously after the specified delay
     * is up. The scheduled task will continue running at the specified
     * interval. The interval will not begin to count down until the last task
     * invocation is complete.
     *
     * @param owner the plugin owning this task
     * @param task the task to run
     * @param delay the delay before this task will be executed
     * @param period the interval before subsequent executions of this task
     * @param unit the unit in which the delay and period will be measured
     * @return the scheduled task
     */
    ScheduledTask schedule(Plugin owner, Runnable task, long delay, long period, TimeUnit unit);

    /**
     * Get the unsafe methods of this class.
     *
     * @return the unsafe method interface
     */
    Unsafe unsafe();

    interface Unsafe
    {

        /**
         * An executor service which underlies this scheduler.
         *
         * @return the underlying executor service or compatible wrapper
         */
        ExecutorService getExecutorService(Plugin plugin);
    }
}
