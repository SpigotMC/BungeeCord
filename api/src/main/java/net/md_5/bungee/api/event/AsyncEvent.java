package net.md_5.bungee.api.event;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Represents an event which depends on the result of asynchronous operations.
 *
 * @param <T> Type of this event
 */
@Data
@Getter(AccessLevel.NONE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AsyncEvent<T> extends Event
{

    private final Callback<T> done;
    private final Map<Plugin, AtomicInteger> intents = new ConcurrentHashMap<>();
    private final AtomicBoolean fired = new AtomicBoolean();
    private final AtomicInteger latch = new AtomicInteger();

    @Override
    @SuppressWarnings("unchecked")
    public void postCall()
    {
        if ( latch.get() == 0 )
        {
            done.done( (T) this, null );
        }
        fired.set( true );
    }

    /**
     * Register an intent that this plugin will continue to perform work on a
     * background task, and wishes to let the event proceed once the registered
     * background task has completed. Multiple intents can be registered by a
     * plugin, but the plugin must complete the same amount of intents for the
     * event to proceed.
     *
     * @param plugin the plugin registering this intent
     */
    public void registerIntent(Plugin plugin)
    {
        Preconditions.checkState( !fired.get(), "Event %s has already been fired", this );

        AtomicInteger intentCount = intents.get( plugin );
        if ( intentCount == null )
        {
            intents.put( plugin, new AtomicInteger( 1 ) );
        } else
        {
            intentCount.incrementAndGet();
        }
        latch.incrementAndGet();
    }

    /**
     * Notifies this event that this plugin has completed an intent and wishes
     * to let the event proceed once all intents have been completed.
     *
     * @param plugin a plugin which has an intent registered for this event
     */
    @SuppressWarnings("unchecked")
    public void completeIntent(Plugin plugin)
    {
        AtomicInteger intentCount = intents.get( plugin );
        Preconditions.checkState( intentCount != null && intentCount.get() > 0, "Plugin %s has not registered intents for event %s", plugin, this );

        intentCount.decrementAndGet();
        if ( fired.get() )
        {
            if ( latch.decrementAndGet() == 0 )
            {
                done.done( (T) this, null );
            }
        } else
        {
            latch.decrementAndGet();
        }
    }
}
