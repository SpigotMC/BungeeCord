package net.md_5.bungee.api.event;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Describes a Cancellable event that can have an attached component message on cancel, for example to disconnect the
 * player.
 */
public interface LoginCancellable extends Cancellable {
    void setCancelReason(BaseComponent... components);

    BaseComponent[] getCancelReasonComponents();
}
