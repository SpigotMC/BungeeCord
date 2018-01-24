package ru.leymooo.botfilter.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;
import net.md_5.bungee.api.plugin.Cancellable;

/**
 * Event called when a player starting checking for a bot
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class BotFilterCheckStartingEvent extends AsyncEvent<BotFilterCheckStartingEvent> implements Cancellable
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
     * A player who starts checking for a bot
     */
    private final ProxiedPlayer player;

    public BotFilterCheckStartingEvent(ProxiedPlayer player, Callback<BotFilterCheckStartingEvent> done)
    {
        super( done );
        this.player = player;
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
