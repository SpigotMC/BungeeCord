package net.md_5.bungee;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.yaml.snakeyaml.Yaml;

public class YamlReconnectHandler implements ReconnectHandler
{

    private final Yaml yaml = new Yaml();
    private final File file = new File( "locations.yml" );
    /*========================================================================*/
    private Map<String, String> data;

    @SuppressWarnings("unchecked")
    public YamlReconnectHandler()
    {
        try
        {
            file.createNewFile();
            try ( FileReader rd = new FileReader( file ) )
            {
                data = yaml.loadAs( rd, Map.class );
            }
        } catch ( IOException ex )
        {
            //ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load reconnect locations", ex ); - Comment out original
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "再接続座標の読み込みに失敗しました。", ex );
        }

        if ( data == null )
        {
            data = new HashMap<>();
        }
    }

    @Override
    public ServerInfo getServer(ProxiedPlayer player)
    {
        ListenerInfo listener = player.getPendingConnection().getListener();
        String name;
        String forced = listener.getForcedHosts().get( player.getPendingConnection().getVirtualHost().getHostName().toLowerCase() );
        if ( forced == null && listener.isForceDefault() )
        {
            forced = listener.getDefaultServer();
        }

        String server = ( forced == null ) ? data.get( key( player ) ) : forced;
        name = ( server != null ) ? server : listener.getDefaultServer();
        ServerInfo info = ProxyServer.getInstance().getServerInfo( name );
        if ( info == null )
        {
            info = ProxyServer.getInstance().getServerInfo( listener.getDefaultServer() );
        }
        //Preconditions.checkState( info != null, "Default server not defined" ); - Comment out original
        Preconditions.checkState( info != null, "デフォルトサーバーが未定義です。" );
        return info;
    }

    @Override
    public void setServer(ProxiedPlayer player)
    {
        data.put( key( player ), player.getServer().getInfo().getName() );
    }

    private String key(ProxiedPlayer player)
    {
        InetSocketAddress host = player.getPendingConnection().getVirtualHost();
        return player.getName() + ";" + host.getHostString() + ":" + host.getPort();
    }

    @Override
    public void save()
    {
        try ( FileWriter wr = new FileWriter( file ) )
        {
            yaml.dump( data, wr );
        } catch ( IOException ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not save reconnect locations", ex );
        }
    }
}
