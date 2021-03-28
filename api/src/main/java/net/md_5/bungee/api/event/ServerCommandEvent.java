package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called when the console executes a command.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class ServerCommandEvent extends Event
{

    /**
     * Issued command.
     */
    private final String command;

    /**
     * Message which will be sent to console when executed command does not exist.
     */
    private String commandNotFoundMessage = "Command not found";

    /**
     * Cancelled state.
     */
    private boolean isCancelled = false;

    public ServerCommandEvent(String command)
    {
        this.command = command;
    }

}
