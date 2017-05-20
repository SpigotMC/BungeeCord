package ru.leymooo.captcha;

import io.netty.channel.Channel;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.PlayerLook;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.TeleportConfirm;

/**
 *
 * @author Leymooo
 */
public class PacketReciever extends PacketHandler
{

    @Getter
    @Setter
    private double joinTime = System.currentTimeMillis();
    @Getter
    private CaptchaUser user;
    @Getter
    private ProtocolTester pt;
    @Getter
    private UserConnection connection;
    @Getter
    private static Random random = new Random();
    @Getter
    private Configuration conf;

    public PacketReciever(Configuration conf, UserConnection con)
    {
        this.conf = conf;
        this.connection = con;
        this.pt = new ProtocolTester( this );
        this.user = new CaptchaUser( this );
        this.conf.getConnectedUsersSet().add( this );
        this.user.sendJoinPackets();
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        this.user.captchaEnter( chat );
    }

    @Override
    public void handle(ConfirmTransaction transaction) throws Exception
    {
        getPt().setTransaction( true );
    }

    @Override
    public void handle(PlayerLook look) throws Exception
    {
        Channel channel = this.getConnection().getCh().getHandle();
        int protocol = this.getConnection().getPendingConnection().getHandshake().getProtocolVersion();
        int positionId = protocol > 47 ? 0x2E : 8;
        this.user.write( channel, this.getPt().getPlayerPositionPacket(), protocol, positionId );
        channel.flush();
    }

    @Override
    public void handle(ClientSettings settings)
    {
        getPt().setSettings( true );
        getConnection().setSettings( settings );
    }

    @Override
    public void handle(PluginMessage message)
    {
        if ( message.getTag().equals( "MC|Brand" ) )
        {
            getPt().setMcbrand( true );
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        if ( alive.getRandomId() == getPt().getKeepAlivePacket().getRandomId() )
        {
            getPt().setAlive( true );
        }
    }

    @Override
    public void handle(TeleportConfirm tp)
    {
        if ( tp.getTeleportId() == getPt().getPlayerPositionPacket().getTeleportId() )
        {
            getPt().setTpconfirm( true );
        }
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        Configuration.getInstance().getConnectedUsersSet().remove( this );
        this.getConnection().disconnect( Util.exception( t ) );

    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        Configuration.getInstance().getConnectedUsersSet().remove( this );
        BungeeCord.getInstance().getPluginManager().callEvent( new PlayerDisconnectEvent( getConnection() ) );
    }

    public void finish()
    {
        this.getConnection().serverr = true;
        this.conf.getConnectedUsersSet().remove( this );
        this.conf.saveIp( getConnection().getName(), getConnection().getAddress().getAddress().getHostAddress() );
        ( (HandlerBoss) this.getConnection().getCh().getHandle().pipeline().get( HandlerBoss.class ) ).setHandler( new UpstreamBridge( ProxyServer.getInstance(), this.getConnection() ) );
        ProxyServer.getInstance().getPluginManager().callEvent( new PostLoginEvent( this.getConnection() ) );
        this.getConnection().connect( ProxyServer.getInstance().getServerInfo( this.getConnection().getPendingConnection().getListener().getDefaultServer() ), null, true );
        clearLinks();
    }

    private void clearLinks()
    {
        connection = null;
        user.setPr( null );
        pt.setPr( null );
        user = null;
        pt = null;
        conf = null;
    }

    @Override
    public String toString()
    {
        return "[" + this.getConnection().getName() + "] <-> Captcha ";
    }

}
