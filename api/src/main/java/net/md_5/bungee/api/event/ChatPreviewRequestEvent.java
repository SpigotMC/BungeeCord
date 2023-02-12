package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the client is requesting a chat preview as a player is preparing a chat message.
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ChatPreviewRequestEvent extends Event
{

    /**
     * The player that is typing a chat message.
     */
    private final ProxiedPlayer player;

    /**
     * The message the player has typed so far.
     */
    private final String message;

    /**
     * The preview message. If left as null, the request will be forwarded to the backend server.
     */
    private BaseComponent preview;
}
