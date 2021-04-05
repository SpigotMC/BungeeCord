package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event called when a command has been executed
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class CommandEvent extends Event
{

    /**
     * CommandSender who issued the command.
     */
    private final CommandSender commandSender;

    /**
     * Issued command.
     */
    private final String command;

    /**
     * Message which will be sent to sender when executed command is cancelled.
     */
    private String commandCancelledMessage = "Command not found";

    /**
     * Message which will be sent to the sender when executed command is forbidden.
     */
    private String notPermittedMessage;

    /**
     * Message which will be sent to the sender when executed command is unknown.
     */
    private String notFoundMessage = "Command not found";

    /**
     * Suppress messages state.
     */
    private boolean suppressMessages = false;

    /**
     * Cancelled state.
     */
    private boolean isCancelled = false;

    public CommandEvent(CommandSender commandSender, String command)
    {
        this.commandSender = commandSender;
        this.command = command;
    }
}
