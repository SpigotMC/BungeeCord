package net.md_5.bungee.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class QueryResponse
{

    // Basic & full query
    private String motd;
    private int playerCount;
    private int maxPlayers;
    private int port;
    private String address;

    // full query only
    private String version;
    private String world = "BungeeCord_Proxy";
    private String server = "BungeeCord";
    @Setter(AccessLevel.NONE)
    private List<String> plugins = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private List<String> players = new ArrayList<>();

    public QueryResponse( ProxyServer server, ListenerInfo listener )
    {
        motd = listener.getMotd();
        playerCount = server.getOnlineCount();
        maxPlayers = listener.getMaxPlayers();
        port = listener.getHost().getPort();
        address = listener.getHost().getHostString();
        version = server.getGameVersion();

        for ( ProxiedPlayer player : server.getPlayers() )
        {
            players.add( player.getName() );
        }
    }
}
