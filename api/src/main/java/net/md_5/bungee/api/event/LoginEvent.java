package net.md_5.bungee.api.event;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Event called to represent a player logging in.
 */
@Getter
@Setter
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
    @NotNull
    @Setter(AccessLevel.NONE)
    private BaseComponent[] cancelReasonComponents;
    /**
     * Connection attempting to login.
     */
    @NotNull
    private final PendingConnection connection;

    public LoginEvent(@NotNull PendingConnection connection, Callback<LoginEvent> done)
    {
        super( done );
        this.connection = connection;
    }

    /**
     * @return reason to be displayed
     * @deprecated Use component methods instead.
     */
    @NotNull
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
    public void setCancelReason(@NotNull String cancelReason)
    {
        setCancelReason( TextComponent.fromLegacyText( cancelReason ) );
    }

    public void setCancelReason(@NotNull BaseComponent... cancelReason)
    {
        this.cancelReasonComponents = cancelReason;
    }
}
