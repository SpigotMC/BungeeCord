package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * This event is called when a command is run.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class CommandEvent extends Event implements Cancellable
{

    /**
     * CommandSender who issued the command.
     */
    private final CommandSender commandSender;

    /**
     * Issued command.
     */
    private String command;

    /**
     * Sent to sender when executed command is cancelled.
     */
    private BaseComponent cancelledMessage;

    /**
     * Sent to the sender when sender has no permission to execute the command.
     */
    private BaseComponent notPermittedMessage;

    /**
     * Sent to the sender when executed command is unknown.
     */
    private BaseComponent notFoundMessage;

    /**
     * If true, no messages will be displayed to the sender.
     */
    private boolean suppressMessages = false;

    /**
     * Cancelled state.
     */
    private boolean cancelled = false;

    public CommandEvent(CommandSender commandSender, String command)
    {
        this.commandSender = commandSender;
        this.command = command;
    }

    /**
     * Get the command sender.
     *
     * @return The sender
     */
    public CommandSender getSender()
    {
        return this.commandSender;
    }

    /**
     * Get the command.
     *
     * @return The command
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * Sets the command that the server will execute.
     *
     * @param command New command that the server will execute
     */
    public void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * Sets the message that will be sent to the sender if the event is cancelled.
     *
     * @param cancelledMessage New message that will be sent to the sender if the event is cancelled
     */
    public void setCancelledMessage(BaseComponent cancelledMessage)
    {
        this.cancelledMessage = cancelledMessage;
    }

    /**
     * Sets the message that will be sent to the sender if the sender is not permitted to execute the command.
     *
     * @param notPermittedMessage New message that will be sent to the sender if the sender is not permitted to execute the command.
     */
    public void setNotPermittedMessage(BaseComponent notPermittedMessage)
    {
        this.notPermittedMessage = notPermittedMessage;
    }

    /**
     * Sets the message that will be sent to the sender if the command could not be found.
     *
     * @param notFoundMessage New message that will be sent to the sender if the command could not be found.
     */
    public void setNotFoundMessage(BaseComponent notFoundMessage)
    {
        this.notFoundMessage = notFoundMessage;
    }
}
