package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.entitymap.EntityMap;
import net.md_5.bungee.forge.ForgeClientHandler;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.tab.Global;
import net.md_5.bungee.tab.GlobalPing;
import net.md_5.bungee.tab.ServerUnique;
import net.md_5.bungee.tab.TabList;
import net.md_5.bungee.util.CaseInsensitiveSet;

@RequiredArgsConstructor
public final class UserConnection implements ProxiedPlayer
{

    /*========================================================================*/
    @NonNull
    private final ProxyServer bungee;
    @NonNull
    private final ChannelWrapper ch;
    @Getter
    @NonNull
    private final String name;
    @Getter
    private final InitialHandler pendingConnection;
    /*========================================================================*/
    @Getter
    @Setter
    private ServerConnection server;
    @Getter
    @Setter
    private boolean dimensionChange = true;
    @Getter
    private final Collection<ServerInfo> pendingConnects = new HashSet<>();
    /*========================================================================*/
    @Getter
    @Setter
    private int sentPingId;
    @Getter
    @Setter
    private long sentPingTime;
    @Getter
    @Setter
    private int ping = 100;
    @Getter
    @Setter
    private ServerInfo reconnectServer;
    @Getter
    private TabList tabListHandler;
    @Getter
    @Setter
    private int gamemode;
    @Getter
    private int compressionThreshold = -1;
    /*========================================================================*/
    private final Collection<String> groups = new CaseInsensitiveSet();
    private final Collection<String> permissions = new CaseInsensitiveSet();
    /*========================================================================*/
    @Getter
    @Setter
    private int clientEntityId;
    @Getter
    @Setter
    private int serverEntityId;
    @Getter
    private ClientSettings settings;
    @Getter
    private final Scoreboard serverSentScoreboard = new Scoreboard();
    /*========================================================================*/
    @Getter
    private String displayName;
    @Getter
    private EntityMap entityRewrite;
    private Locale locale;
    /*========================================================================*/
    @Getter
    @Setter
    private ForgeClientHandler forgeClientHandler;
    @Getter
    @Setter
    private ForgeServerHandler forgeServerHandler;
    /*========================================================================*/
    private final Unsafe unsafe = new Unsafe()
    {
        @Override
        public void sendPacket(DefinedPacket packet)
        {
            ch.write( packet );
        }
    };

    public void init()
    {
        this.entityRewrite = EntityMap.getEntityMap( getPendingConnection().getVersion() );

        this.displayName = name;

        // Blame Mojang for this one
        /*switch ( getPendingConnection().getListener().getTabListType() )
        {
            case "GLOBAL":
                tabListHandler = new Global( this );
                break;
            case "SERVER":
                tabListHandler = new ServerUnique( this );
                break;
            default:
                tabListHandler = new GlobalPing( this );
                break;
        }*/
        tabListHandler = new ServerUnique( this );

        Collection<String> g = bungee.getConfigurationAdapter().getGroups( name );
        for ( String s : g )
        {
            addGroups( s );
        }

        forgeClientHandler = new ForgeClientHandler( this );
    }

    public void sendPacket(PacketWrapper packet)
    {
        ch.write( packet );
    }

    @Deprecated
    public boolean isActive()
    {
        return !ch.isClosed();
    }

    @Override
    public void setDisplayName(String name)
    {
        Preconditions.checkNotNull( name, "displayName" );
        Preconditions.checkArgument( name.length() <= 16, "Display name cannot be longer than 16 characters" );
        displayName = name;
    }

    @Override
    public void connect(ServerInfo target)
    {
        connect( target, null );
    }

    @Override
    public void connect(ServerInfo target, Callback<Boolean> callback)
    {
        connect( target, callback, false );
    }

    void sendDimensionSwitch()
    {
        dimensionChange = true;
        unsafe().sendPacket( PacketConstants.DIM1_SWITCH );
        unsafe().sendPacket( PacketConstants.DIM2_SWITCH );
    }

    public void connectNow(ServerInfo target)
    {
        sendDimensionSwitch();
        connect( target );
    }

    public void connect(ServerInfo info, final Callback<Boolean> callback, final boolean retry)
    {
        Preconditions.checkNotNull( info, "info" );

        ServerConnectEvent event = new ServerConnectEvent( this, info );
        if ( bungee.getPluginManager().callEvent( event ).isCancelled() )
        {
            return;
        }

        final BungeeServerInfo target = (BungeeServerInfo) event.getTarget(); // Update in case the event changed target

        if ( getServer() != null && Objects.equals( getServer().getInfo(), target ) )
        {
            sendMessage( bungee.getTranslation( "already_connected" ) );
            return;
        }
        if ( pendingConnects.contains( target ) )
        {
            sendMessage( bungee.getTranslation( "already_connecting" ) );
            return;
        }

        pendingConnects.add( target );

        ChannelInitializer initializer = new ChannelInitializer()
        {
            @Override
            protected void initChannel(Channel ch) throws Exception
            {
                PipelineUtils.BASE.initChannel( ch );
                ch.pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder( Protocol.HANDSHAKE, false, getPendingConnection().getVersion() ) );
                ch.pipeline().get( HandlerBoss.class ).setHandler( new ServerConnector( bungee, UserConnection.this, target ) );
            }
        };
        ChannelFutureListener listener = new ChannelFutureListener()
        {
            @Override
            @SuppressWarnings("ThrowableResultIgnored")
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( callback != null )
                {
                    callback.done( future.isSuccess(), future.cause() );
                }

                if ( !future.isSuccess() )
                {
                    future.channel().close();
                    pendingConnects.remove( target );

                    ServerInfo def = ProxyServer.getInstance().getServers().get( getPendingConnection().getListener().getFallbackServer() );
                    if ( retry && target != def && ( getServer() == null || def != getServer().getInfo() ) )
                    {
                        sendMessage( bungee.getTranslation( "fallback_lobby" ) );
                        connect( def, null, false );
                    } else
                    {
                        if ( dimensionChange )
                        {
                            disconnect( bungee.getTranslation( "fallback_kick" ) + future.cause().getClass().getName() );
                        } else
                        {
                            sendMessage( bungee.getTranslation( "fallback_kick" ) + future.cause().getClass().getName() );
                        }
                    }
                }
            }
        };
        Bootstrap b = new Bootstrap()
                .channel( PipelineUtils.getChannel() )
                .group( ch.getHandle().eventLoop() )
                .handler( initializer )
                .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
                .remoteAddress( target.getAddress() );
        // Windows is bugged, multi homed users will just have to live with random connecting IPs
        if ( getPendingConnection().getListener().isSetLocalAddress() && !PlatformDependent.isWindows() )
        {
            b.localAddress( getPendingConnection().getListener().getHost().getHostString(), 0 );
        }
        b.connect().addListener( listener );
    }

    @Override
    public void disconnect(String reason)
    {
        disconnect0( TextComponent.fromLegacyText( reason ) );
    }

    @Override
    public void disconnect(BaseComponent... reason)
    {
        disconnect0( reason );
    }

    @Override
    public void disconnect(BaseComponent reason)
    {
        disconnect0( reason );
    }

    public void disconnect0(final BaseComponent... reason)
    {
        if ( !ch.isClosed() )
        {
            bungee.getLogger().log( Level.INFO, "[{0}] disconnected with: {1}", new Object[]
            {
                getName(), BaseComponent.toLegacyText( reason )
            } );

            // Why do we have to delay this you might ask? Well the simple reason is MOJANG.
            // Despite many a bug report posted, ever since the 1.7 protocol rewrite, the client STILL has a race condition upon switching protocols.
            // As such, despite the protocol switch packets already having been sent, there is the possibility of a client side exception
            // To help combat this we will wait half a second before actually sending the disconnected packet so that whoever is on the other
            // end has a somewhat better chance of receiving the proper packet.
            ch.getHandle().eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    unsafe().sendPacket( new Kick( ComponentSerializer.toString( reason ) ) );
                    ch.close();
                }
            }, 500, TimeUnit.MILLISECONDS );

            if ( server != null )
            {
                server.disconnect( "Quitting" );
            }
        }
    }

    @Override
    public void chat(String message)
    {
        Preconditions.checkState( server != null, "Not connected to server" );
        server.getCh().write( new Chat( message ) );
    }

    @Override
    public void sendMessage(String message)
    {
        sendMessage( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void sendMessages(String... messages)
    {
        for ( String message : messages )
        {
            sendMessage( message );
        }
    }

    @Override
    public void sendMessage(BaseComponent... message)
    {
        unsafe().sendPacket( new Chat( ComponentSerializer.toString( message ) ) );
    }

    @Override
    public void sendMessage(BaseComponent message)
    {
        unsafe().sendPacket( new Chat( ComponentSerializer.toString( message ) ) );
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        unsafe().sendPacket( new PluginMessage( channel, data, forgeClientHandler.isForgeUser() ) );
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) ch.getHandle().remoteAddress();
    }

    @Override
    public Collection<String> getGroups()
    {
        return Collections.unmodifiableCollection( groups );
    }

    @Override
    public void addGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.add( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, true );
            }
        }
    }

    @Override
    public void removeGroups(String... groups)
    {
        for ( String group : groups )
        {
            this.groups.remove( group );
            for ( String permission : bungee.getConfigurationAdapter().getPermissions( group ) )
            {
                setPermission( permission, false );
            }
        }
    }

    @Override
    public boolean hasPermission(String permission)
    {
        return bungee.getPluginManager().callEvent( new PermissionCheckEvent( this, permission, permissions.contains( permission ) ) ).hasPermission();
    }

    @Override
    public void setPermission(String permission, boolean value)
    {
        if ( value )
        {
            permissions.add( permission );
        } else
        {
            permissions.remove( permission );
        }
    }

    @Override
    public Collection<String> getPermissions()
    {
        return Collections.unmodifiableCollection( permissions );
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public Unsafe unsafe()
    {
        return unsafe;
    }

    @Override
    public String getUUID()
    {
        return getPendingConnection().getUUID();
    }

    @Override
    public UUID getUniqueId()
    {
        return getPendingConnection().getUniqueId();
    }

    public void setSettings(ClientSettings settings)
    {
        this.settings = settings;
        this.locale = null;
    }

    @Override
    public Locale getLocale()
    {
        return ( locale == null && settings != null ) ? locale = Locale.forLanguageTag( settings.getLocale().replaceAll( "_", "-" ) ) : locale;
    }
    
    @Override 
    public Map<String, String> getModList()
    {  
        if (forgeClientHandler.getClientModList() == null)
        {
            // Return an empty map, rather than a null, if the client hasn't got any mods,
            // or is yet to complete a handshake.
            return ImmutableMap.of();
        }
        
        return ImmutableMap.copyOf( forgeClientHandler.getClientModList() );
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        if ( this.compressionThreshold == -1 )
        {
            this.compressionThreshold = compressionThreshold;
            unsafe.sendPacket( new SetCompression( compressionThreshold ) );
            ch.setCompressionThreshold( compressionThreshold );
        }
    }
}
