package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/** Called when the player is disconnected from the instance.
 * If the player is kicked from the instance, ServerKickEvent will instead be called.
 */
@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class ServerDisconnectEvent extends Event
{

    /**
     * Player disconnecting from a server.
     */
    @NonNull
    private final ProxiedPlayer player;
    /**
     * Server the player is disconnecting from.
     */
    @NonNull
    private final ServerInfo target;
}
