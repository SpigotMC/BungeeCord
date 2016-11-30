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
 * Event called to represent a player logging in.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LoginEvent extends AsyncEvent<LoginEvent> implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Message to use when kicking if this event is canceled.
     */
    @Setter(AccessLevel.NONE)
    private BaseComponent[] cancelReasonComponent;
    /**
     * Connection attempting to login.
     */
    private final PendingConnection connection;

    public LoginEvent(PendingConnection connection, Callback<LoginEvent> done)
    {
        super( done );
        this.connection = connection;
    }

    @Deprecated
    public String getCancelReason()
    {
        return BaseComponent.toLegacyText( cancelReasonComponent );
    }

    @Deprecated
    public void setCancelReason(String reason)
    {
        cancelReasonComponent = TextComponent.fromLegacyText( reason );
    }

    public void setCancelReasonComponent(BaseComponent... reason) {
        cancelReasonComponent = reason;
    }
}
