package ru.leymooo.captcha;

import java.io.BufferedWriter;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.extra.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.extra.PlayerLook;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionRotation;
import net.md_5.bungee.protocol.packet.extra.TeleportConfirm;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Leymooo
 */
public class CaptchaConnector extends PacketHandler
{

    @Getter
    private double joinTime = System.currentTimeMillis();
    @Getter
    private FakeServer userServer;
    @Getter
    private UserConnection userConnection;
    //=====================================================================
    private boolean settings = false;
    @Setter
    private boolean tpconfirm = false;
    private boolean mcbrand = false;
    private boolean alive = false;
    private boolean transaction = false;
    private boolean posRot = false;
    //====================================================================
    BufferedWriter writer = null;
    private String playerName;

    public CaptchaConnector(UserConnection con)
    {
        this.userConnection = con;
        this.playerName = this.userConnection.getName();
        this.userServer = new FakeServer( this );
        Configuration.getInstance().getConnectedUsersSet().add( this );
        this.userServer.sendJoinPackets();
        this.sendProgressBar();
        BungeeCord.getInstance().getLogger().log( Level.INFO, "{0} has connected", this );
    }

    @Override
    public void handle(Chat chat) throws Exception
    {
        this.userServer.captchaEnter( chat );
    }

    @Override
    public void handle(ConfirmTransaction transaction) throws Exception
    {
        if ( transaction.isAccepted() && ( transaction.getAction() == this.getUserServer().getTransactionPacket().getAction() ) )
        {
            this.transaction = true;
        }
    }

    @Override
    public void handle(PlayerLook look) throws Exception
    {
        this.getUserConnection().unsafe().sendPacket( this.getUserServer().getPlayerPositionPacket() );
    }

    @Override
    public void handle(ClientSettings settings)
    {
        this.settings = true;
        this.getUserConnection().setSettings( settings );
    }

    @Override
    public void handle(PluginMessage message)
    {
        if ( message.getTag().equals( "MC|Brand" ) )
        {
            this.mcbrand = true;
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        if ( alive.getRandomId() == this.getUserServer().getKeepAlivePacket().getRandomId() )
        {
            this.alive = true;
        }
    }

    @Override
    public void handle(TeleportConfirm tp)
    {
        if ( !this.tpconfirm && tp.getTeleportId() == this.getUserServer().getPlayerPositionPacket().getTeleportId() )
        {
            this.tpconfirm = true;
        }
    }

    @Override
    public void handle(PlayerPositionRotation posRot) throws Exception
    {
        PlayerPositionRotation pos = this.getUserServer().getPlayerPositionPacket();
        if ( !this.posRot && pos.getX() == posRot.getX() && pos.getY() == posRot.getY() && pos.getZ() == posRot.getZ() && pos.getYaw() == posRot.getYaw() && pos.getPitch() == posRot.getPitch() && pos.isOnGround() == posRot.isOnGround() )
        {
            this.posRot = true;
        }
    }

    public boolean isBot()
    {
        return !( this.settings && this.tpconfirm && this.mcbrand && this.alive && this.transaction && this.posRot );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        this.getUserConnection().disconnect( Util.exception( t ) );

    }

    @Override
    public void disconnected(ChannelWrapper channel) throws Exception
    {
        Configuration.getInstance().getConnectedUsersSet().remove( this );
        BungeeCord.getInstance().getPluginManager().callEvent( new PlayerDisconnectEvent( this.getUserConnection() ) );
        this.clearLinks();
    }

    public void finish()
    {
        Configuration conf = Configuration.getInstance();
        this.getUserConnection().serverr = true;
        conf.getConnectedUsersSet().remove( this );
        conf.saveIp( this.getUserConnection().getName(), this.getUserConnection().getAddress().getAddress().getHostAddress() );
        ( (HandlerBoss) this.getUserConnection().getCh().getHandle().pipeline().get( HandlerBoss.class ) ).setHandler( new UpstreamBridge( ProxyServer.getInstance(), this.getUserConnection() ) );
        ProxyServer.getInstance().getPluginManager().callEvent( new PostLoginEvent( this.getUserConnection() ) );
        this.getUserConnection().connect( ProxyServer.getInstance().getServerInfo( this.getUserConnection().getPendingConnection().getListener().getDefaultServer() ), null, true );
        this.clearLinks();
    }

    private void clearLinks()
    {
        userConnection = null;
        userServer.setReciever( null );
        userServer = null;
    }

    //Автор идеи - https://vk.com/demirug
    public void sendProgressBar()
    {
        double startValue = Configuration.getInstance().getTimeout();
        double currentValue = System.currentTimeMillis() - this.getJoinTime();
        int lenght = (int) Math.round( startValue / 1000 );
        int progress1 = (int) Math.round( currentValue / startValue * lenght );
        String output = "§a§l".concat( StringUtils.repeat( "\u2588", ( progress1 ) )
                .concat( "§c§l" ).concat( StringUtils.repeat( "\u2588", ( lenght - progress1 ) ) ) );
        BaseComponent[] comp = TextComponent.fromLegacyText( output );
        this.getUserConnection().sendMessage( ChatMessageType.ACTION_BAR, ComponentSerializer.toString( new TextComponent( BaseComponent.toLegacyText( comp ) ) ) );
    }
    //

    @Override
    public String toString()
    {
        return "[" + this.playerName + "] <-> CaptchaConnector";
    }
}
