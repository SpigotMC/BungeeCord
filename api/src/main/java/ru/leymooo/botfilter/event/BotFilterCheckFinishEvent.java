package ru.leymooo.botfilter.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;
import ru.leymooo.botfilter.CheckState;

/**
 * Called when a player has finished bot check, it is not safe to call any
 * methods that perform an action on the passed player instance if result is not
 * successful.
 *
 * Event will be dispatched before LoginEvent if result is successful.
 *
 * If result is not successful a PlayerDisconnectEvent will be dispatched
 * anyway.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class BotFilterCheckFinishEvent extends Event
{

    /**
     * Player that finished bot check.
     */
    private final ProxiedPlayer player;

    /**
     * Result of the bot check
     */
    private final CheckState checkState;

}
