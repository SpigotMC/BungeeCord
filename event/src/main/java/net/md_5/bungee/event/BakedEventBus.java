package net.md_5.bungee.event;

/**
 * This class is an extension of {@link EventBus}, however it includes a number
 * of extra methods to facilitate the "baking" of event handlers. Baking is an
 * expensive process called after registering event handlers which may optimize
 * the internal handler implementation for efficiency. Examples of baking
 * processes could include flattening a map or higher order data structure into
 * an array, or compiling the event dispatch methods into bytecode.
 */
public interface BakedEventBus extends EventBus
{

    /**
     * Run the baking process on all registered handlers.
     */
    void bake();

    /**
     * Run the baking process on all registered handlers for the specified event
     * class.
     *
     * @param eventClass the class to bake handlers for.
     */
    void bake(Class<?> eventClass);

    /**
     * Return whether or not the handler will auto bake events after each
     * handler registration.
     *
     * @return the auto bake status
     */
    boolean isAutoBake();

    /**
     * Sets the auto bake status of this handler. See {@link #isAutoBake()} for
     * further information.
     *
     * @param autoBake whether to auto bake or not
     */
    void setAutoBake(boolean autoBake);
}
