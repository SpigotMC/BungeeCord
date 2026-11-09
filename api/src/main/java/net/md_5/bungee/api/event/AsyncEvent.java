package net.md_5.bungee.api.event;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.*;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.AsyncEventBusEvent;
import net.md_5.bungee.event.AsyncEventContext;
/**
 * Represents an event which depends on the result of asynchronous operations.
 *
 * @param <T> Type of this event
 */
@Data
@Getter(AccessLevel.NONE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AsyncEvent<T> extends Event implements AsyncEventBusEvent
{

    private final Callback<T> done;
    private volatile boolean completed = false;
    private final AtomicBoolean intent = new AtomicBoolean();
    @Setter
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AsyncEventContext<?> asyncEventContext;

    @Override
    @SuppressWarnings("unchecked")
    public void onComplete()
    {
        Preconditions.checkState( !completed, "Event %s has already been fired", this );
        completed = true;

        done.done( (T) this, null );
    }

    /**
     * Register an intent that this plugin will continue to perform work on a
     * background task, and wishes to let the event proceed once the registered
     * background task has completed. Multiple intents can be registered by a
     * plugin, but the plugin must complete the same amount of intents for the
     * event to proceed.
     *
     * @param plugin the plugin registering this intent
     * @deprecated use {@link #registerIntent()} since intent now has an execution order
     */
    @Deprecated
    public void registerIntent(Plugin plugin)
    {
        registerIntent();
    }

    /**
     * Notifies this event that this plugin has completed an intent and wishes
     * to let the event proceed once all intents have been completed.
     *
     * @param plugin a plugin which has an intent registered for this event
     * @deprecated use {@link #completeIntent()} since intent now has an execution order
     */
    @Deprecated
    public void completeIntent(Plugin plugin)
    {
        completeIntent();
    }

    /**
     * Register an intent that this plugin will continue to perform work on a
     * background task, and wishes to let the event proceed once the registered
     * background task has completed. The plugin must complete the intent for the
     * event to proceed.
     */
    @Override
    public void registerIntent()
    {
        Preconditions.checkState( !completed, "Event %s has already been fired", this );
        Preconditions.checkState( !intent.get(), "Intent has already been registered for the event %s", this );

        intent.set( true );
    }

    /**
     * Checks if the current event is in the intent state.
     * @return intent state
     */
    @Override
    public boolean isRegisteredIntent()
    {
        return intent.get();
    }

    /**
     * Notifies this event that this plugin has completed an intent and wishes
     * to let the event proceed.
     */
    @Override
    public void completeIntent()
    {
        Preconditions.checkState( !completed, "Event %s has already been fired", this );
        Preconditions.checkState( intent.get(), "Intent has not yet been registered for the event %s", this );

        intent.set( false );

        asyncEventContext.post();
    }

}
