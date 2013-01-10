package net.md_5.bungee.plugin;

/**
 * An event which may be canceled and this be prevented from happening.
 */
public interface Cancellable
{

    /**
     * Sets the canceled state of this event.
     *
     * @param canceled whether this event is canceled or not
     */
    public void setCancelled(boolean canceled);

    /**
     * Gets the canceled state of this event.
     *
     * @return whether this event is canceled or not
     */
    public boolean isCancelled();
}
