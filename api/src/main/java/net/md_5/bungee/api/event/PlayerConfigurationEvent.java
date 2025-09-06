package net.md_5.bungee.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PlayerConfigurationEvent extends AsyncEvent<PlayerConfigurationEvent>
{
    private ProxiedPlayer player;

    public PlayerConfigurationEvent(ProxiedPlayer player, Callback<PlayerConfigurationEvent> done)
    {
        super( done );
        this.player = player;
    }
}
