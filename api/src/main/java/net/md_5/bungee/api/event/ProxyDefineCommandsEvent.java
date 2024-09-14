package net.md_5.bungee.api.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Command;

/**
 * Called when the proxy intercepts the Commands packet, allowing plugins to
 * hide commands that clients have permission for without disabling them.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProxyDefineCommandsEvent extends TargetedEvent
{
    /**
     * The commands to send to the player
     */
    private final Map<String, Command> commands;

    public ProxyDefineCommandsEvent(Connection sender, Connection receiver, Collection<Map.Entry<String, Command>> commands)
    {
        super( sender, receiver );
        this.commands = new HashMap<>( commands.size() );
        for ( Map.Entry<String, Command> command : commands )
        {
            this.commands.put( command.getKey(), command.getValue() );
        }
    }
}
