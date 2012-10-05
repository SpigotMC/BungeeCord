package net.md_5.bungee.command;

public interface CommandSender {

    /**
     * Sends a message to the client at the earliest available opportunity.
     *
     * @param message the message to send
     */
    public abstract void sendMessage(String message);
}
