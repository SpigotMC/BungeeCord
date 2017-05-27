package ru.leymooo.captcha;

import java.awt.print.Book;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
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
import net.md_5.bungee.protocol.packet.extra.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.extra.PlayerLook;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionRotation;
import net.md_5.bungee.protocol.packet.extra.TeleportConfirm;

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
        Configuration.getInstance().getConnectedUsersSet().add( this );
        this.userConnection = con;
        this.playerName = this.userConnection.getName();
        this.userServer = new FakeServer( this );
        if ( Configuration.getInstance().isDebug() )
        {
            this.createFiles();
        }
        this.userServer.sendJoinPackets();
        BungeeCord.getInstance().getLogger().log( Level.INFO, "{0} has connected", this );
    }

    private void createFiles()
    {
        try
        {
            File folder = new File( "debug" );
            if ( !folder.exists() )
            {
                folder.mkdir();
            }
            File playerFile = new File( folder, this.getUserConnection().getName().concat( ".txt" ) );
            if ( !playerFile.exists() )
            {
                playerFile.createNewFile();
            }
            this.writer = new BufferedWriter( new FileWriter( playerFile, true ) );
            this.debugPlayer( "Player joined", false, false );
            this.debugPlayer( "Protocol version is " + this.getUserConnection().getPendingConnection().getHandshake().getProtocolVersion(), false, false );
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.SEVERE, "Какаята ошибка. Напишите сюда vk.com/Leymooo_s", e );
        }
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
            this.debugPlayer( "Recieved and handled correctly: ".concat( transaction.toString() ), false, false );
            this.transaction = true;
        }
    }

    @Override
    public void handle(PlayerLook look) throws Exception
    {
        this.debugPlayer( "Recieved and handled: ".concat( look.toString() ), false, false );
        this.getUserConnection().unsafe().sendPacket( this.getUserServer().getPlayerPositionPacket() );
        this.debugPlayer( "Sended : ".concat( this.getUserServer().getPlayerPositionPacket().toString() ), false, false );
    }

    @Override
    public void handle(ClientSettings settings)
    {
        this.debugPlayer( "Recieved and handled: ".concat( settings.toString() ), false, false );
        this.settings = true;
        this.getUserConnection().setSettings( settings );
    }

    @Override
    public void handle(PluginMessage message)
    {
        if ( message.getTag().equals( "MC|Brand" ) )
        {
            this.debugPlayer( "Recieved and handled correctly: ".concat( message.toString() ), false, false );
            this.mcbrand = true;
        }
    }

    @Override
    public void handle(KeepAlive alive) throws Exception
    {
        if ( alive.getRandomId() == this.getUserServer().getKeepAlivePacket().getRandomId() )
        {
            this.debugPlayer( "Recieved and handled correctly: ".concat( alive.toString() ), false, false );
            this.alive = true;
        }
    }

    @Override
    public void handle(TeleportConfirm tp)
    {
        if ( !this.tpconfirm && tp.getTeleportId() == this.getUserServer().getPlayerPositionPacket().getTeleportId() )
        {
            this.debugPlayer( "Recieved and handled correctly: ".concat( tp.toString() ), false, false );
            this.tpconfirm = true;
        }
    }

    @Override
    public void handle(PlayerPositionRotation posRot) throws Exception
    {
        PlayerPositionRotation pos = this.getUserServer().getPlayerPositionPacket();
        if ( !this.posRot && pos.getX() == posRot.getX() && pos.getY() == posRot.getY() && pos.getZ() == posRot.getZ() && pos.getYaw() == posRot.getYaw() && pos.getPitch() == posRot.getPitch() && pos.isOnGround() == posRot.isOnGround() )
        {
            this.debugPlayer( "Recieved and handled correctly: ".concat( posRot.toString() ), false, false );
            this.posRot = true;
        }
    }

    public boolean isBot()
    {
        return ( System.currentTimeMillis() - this.joinTime >= 6000 ) && !( this.settings && this.tpconfirm && this.mcbrand && this.alive && this.transaction && this.posRot );
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
        this.debugPlayer( String.format( "Player was disconnected. Settings: %s, TPConfirm: %s, MCBrand: %s, KeepAlive: %s, Transaction: %s, PosRot: %s",
                this.settings + "", this.tpconfirm + "", this.mcbrand + "", this.alive + "", this.transaction + "", this.posRot + "" ), true, !this.isBot() );
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
        this.debugPlayer( null, true, true );
        this.clearLinks();
    }

    private void clearLinks()
    {
        userConnection = null;
        userServer.setReciever( null );
        userServer = null;
    }

    @Override
    public String toString()
    {
        return "[" + this.playerName + "] <-> CaptchaConnector";
    }

    public void debugPlayer(String messageToLog, boolean needClose, boolean needDeleteLog)
    {
        try
        {
            if ( writer == null )
            {
                return;
            }
            if ( messageToLog != null )
            {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy H:MM:SS " );
                String formattedDate = "[" + sdf.format( date ) + "] ";
                this.writer.write( formattedDate.concat( messageToLog ).concat( System.getProperty( "line.separator" ) ) );
            }
            if ( needDeleteLog )
            {
                this.writer.flush();
                this.writer.close();
                File f = new File( "debug/".concat( this.getUserConnection().getName() ).concat( ".txt" ) );
                f.delete();
            }
            if ( needClose )
            {
                this.writer.close();
                this.writer = null;
            }
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.SEVERE, "Какаята ошибка. Напишите сюда vk.com/Leymooo_s", e );
        }
    }
}
