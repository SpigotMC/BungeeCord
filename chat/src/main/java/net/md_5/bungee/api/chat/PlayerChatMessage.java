package net.md_5.bungee.api.chat;

public interface PlayerChatMessage
{
    /**
     * Get the message sent by the player.
     *
     * @return the message string sent by the player
     */
    String getMessage();

    /**
     * Get whether this chat packet was signed (1.19+) or not.
     *
     * @return {@code true} if the chat packet was signed by the user
     */
    boolean isSigned();
}
