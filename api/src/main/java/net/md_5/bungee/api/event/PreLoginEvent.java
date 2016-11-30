package net.md_5.bungee.api.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called to represent a player first making their presence and username
 * known.
 *
 * This will NOT contain many attributes relating to the player which are filled
 * in after authentication with Mojang's servers. Examples of attributes which
 * are not available include their UUID.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PreLoginEvent extends AsyncEvent<PreLoginEvent> implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    @Setter(AccessLevel.NONE)
    private BaseComponent[] cancelReasonComponents;
    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;

    public PreLoginEvent(PendingConnection connection, Callback<PreLoginEvent> done)
    {
        super( done );
        this.connection = connection;
    }

    /**
     * @return reason to be displayed
     * @deprecated Use component methods instead.
     */
    @Deprecated
    public String getCancelReason()
    {
        return BaseComponent.toLegacyText( getCancelReasonComponents() );
    }

    /**
     * @param cancelReason reason to be displayed
     * @deprecated Use
     * {@link #setCancelReason(net.md_5.bungee.api.chat.BaseComponent...)}
     * instead.
     */
    @Deprecated
    public void setCancelReason(String cancelReason)
    {
        setCancelReason( TextComponent.fromLegacyText( cancelReason ) );
    }

    public void setCancelReason(BaseComponent... cancelReason)
    {
        this.cancelReasonComponents = cancelReason;
    }
}
