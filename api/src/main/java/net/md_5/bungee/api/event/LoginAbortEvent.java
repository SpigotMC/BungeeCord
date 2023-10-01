package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Event;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class LoginAbortEvent extends Event
{

    /**
     * Connection aborted.
     */
    private final PendingConnection connection;

}
