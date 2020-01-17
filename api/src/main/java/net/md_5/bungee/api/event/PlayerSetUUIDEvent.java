package net.md_5.bungee.api.event;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

/**
 * @author Leymooo
 */
@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerSetUUIDEvent extends Event
{

    private final PendingConnection pendingConnection;

    private final UUID offlineUuid;

    /**
     * Set player UUID before player start a check for a bot. Because botfilter
     * delayed a LoginEvent when player needs a check for a bot, plugins can not
     * change a uniqueId field via reflection in event, because BotFilter needs
     * send a LoginSuccess packet before a LoginEvent will be fired.
     */
    private UUID uniqueId;
}
