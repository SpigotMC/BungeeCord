package net.md_5.bungee.api.event;

import lombok.Data;
import net.md_5.bungee.api.Connection;
import net.md_5.bungee.api.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;

@Data
public class ChatEvent implements Cancellable
{

    /**
     * Cancelled state.
     */
    private boolean cancelled;
    /**
     * Where this message is being sent to.
     */
    private final Connection destination;
    /**
     * User involved with sending or receiving this message.
     */
    private final ProxiedPlayer player;
    /**
     * Text contained in this chat.
     */
    private String message;
}
