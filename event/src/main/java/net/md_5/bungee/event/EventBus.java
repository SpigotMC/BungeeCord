package net.md_5.bungee.event;

/**
 * This class represents a basic event bus. The general general idea of such a
 * class is that objects (listeners) are registered as being able to receive
 * events of a specific class. When such an event is posted, all listeners which
 * listen to that class will receive a notification of the event.
 *
 * In this event bus implementation, all event handling methods must have a
 * single parameter designating the event class which they wish to handle, and
 * an {@link EventHandler} annotation signifying themselves as a handler and
 * providing extra information such as priority.
 */
public interface EventBus
{

    /**
     * Posts an event to this event bus so that is will be received by all
     * registered listeners. This event will not throw any exceptions, so any
     * exceptions thrown by registered listeners must be handled by the
     * implementation.
     *
     * @param event the event to be posted
     */
    void post(Object event);

    /**
     * Register a listener for receiving events. This method will add all
     * methods annotated with {@link EventHandler} to the list of event
     * handlers. Any invalid {@link EventHandler} annotated methods, such as
     * those which are private or have more than a single argument will be
     * ignored.
     *
     * @param listener the object to search for event handling methods
     */
    void register(Object listener);

    /**
     * Unregister a previously registered event listener. This method will do
     * the reverse of {@link #register(java.lang.Object)}, and remove any
     * registered event handling methods belonging to the specified class.
     *
     * @param listener the object to remove from the list of event handlers
     * @throws IllegalArgumentException if the specified listener was not
     * previously registered.
     */
    void unregister(Object listener);
}
