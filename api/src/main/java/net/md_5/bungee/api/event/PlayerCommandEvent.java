package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called when a player executes a command.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class PlayerCommandEvent extends Event
{

    /**
     * Player who issued the command.
     */
    private final ProxiedPlayer player;

    /**
     * Issued command.
     */
    private final String command;

    /**
     * Message which will be sent to the player when executed command is forbidden.
     */
    private String playerNotPermittedMessage;

    public PlayerCommandEvent(ProxiedPlayer player, String command)
    {
        this.player = player;
        this.command = command;
    }

}
