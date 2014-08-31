package net.md_5.bungee;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.module.ModuleManager;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.log.BungeeLogger;
import net.md_5.bungee.scheduler.BungeeScheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.conf.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.internal.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.command.*;
import net.md_5.bungee.conf.YamlConfig;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.log.LoggingOutputStream;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.query.RemoteQuery;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.fusesource.jansi.AnsiConsole;

/**
 * Main BungeeCord proxy class.
 */
public class BungeeCord extends ProxyServer
{

    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    @Getter
    public final Configuration config = new Configuration();
    /**
     * Localization bundle.
     */
    public ResourceBundle bundle;
    public EventLoopGroup eventLoops;
    /**
     * locations.yml save thread.
     */
    private final Timer saveThread = new Timer( "Reconnect Saver" );
    private final Timer metricsThread = new Timer( "Metrics Thread" );
    /**
     * Server socket listener.
     */
    private final Collection<Channel> listeners = new HashSet<>();
    /**
     * Fully qualified connections.
     */
    private final Map<String, UserConnection> connections = new CaseInsensitiveMap<>();
    // Used to help with packet rewriting
    private final Map<UUID, UserConnection> connectionsByOfflineUUID = new HashMap<>();
    private final ReadWriteLock connectionLock = new ReentrantReadWriteLock();
    /**
     * Plugin manager.
     */
    @Getter
    public final PluginManager pluginManager = new PluginManager( this );
    @Getter
    @Setter
    private ReconnectHandler reconnectHandler;
    @Getter
    @Setter
    private ConfigurationAdapter configurationAdapter = new YamlConfig();
    private final Collection<String> pluginChannels = new HashSet<>();
    @Getter
    private final File pluginsFolder = new File( "plugins" );
    @Getter
    private final BungeeScheduler scheduler = new BungeeScheduler();
    @Getter
    private final ConsoleReader consoleReader;
    @Getter
    private final Logger logger;
    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer( ProtocolConstants.MINECRAFT_1_7_6 ) )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    public final Gson gsonLegacy = new GsonBuilder()
            .registerTypeAdapter( ServerPing.PlayerInfo.class, new PlayerInfoSerializer( ProtocolConstants.MINECRAFT_1_7_2 ) )
            .registerTypeAdapter( Favicon.class, Favicon.getFaviconTypeAdapter() ).create();
    @Getter
    private ConnectionThrottle connectionThrottle;
    private final ModuleManager moduleManager = new ModuleManager();


    {
        // TODO: Proper fallback when we interface the manager
        getPluginManager().registerCommand( null, new CommandReload() );
        getPluginManager().registerCommand( null, new CommandEnd() );
        getPluginManager().registerCommand( null, new CommandIP() );
        getPluginManager().registerCommand( null, new CommandBungee() );
        getPluginManager().registerCommand( null, new CommandPerms() );

        registerChannel( "BungeeCord" );
    }

    public static BungeeCord getInstance()
    {
        return (BungeeCord) ProxyServer.getInstance();
    }

    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public BungeeCord() throws IOException
    {
        // Java uses ! to indicate a resource inside of a jar/zip/other container. Running Bungee from within a directory that has a ! will cause this to muck up.
        Preconditions.checkState( new File( "." ).getAbsolutePath().indexOf( '!' ) == -1, "Cannot use BungeeCord in directory with ! in path." );

        System.setSecurityManager( new BungeeSecurityManager() );

        try
        {
            bundle = ResourceBundle.getBundle( "messages" );
        } catch ( MissingResourceException ex )
        {
            bundle = ResourceBundle.getBundle( "messages", Locale.ENGLISH );
        }

        Log.setOutput( new PrintStream( ByteStreams.nullOutputStream() ) ); // TODO: Bug JLine
        AnsiConsole.systemInstall();
        consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents( false );

        logger = new BungeeLogger( this );
        System.setErr( new PrintStream( new LoggingOutputStream( logger, Level.SEVERE ), true ) );
        System.setOut( new PrintStream( new LoggingOutputStream( logger, Level.INFO ), true ) );

        if ( consoleReader.getTerminal() instanceof UnsupportedTerminal )
        {
            logger.info( "Unable to initialize fancy terminal. To fix this on Windows, install the correct Microsoft Visual C++ 2008 Runtime" );
            logger.info( "NOTE: This error is non crucial, and BungeeCord will still function correctly! Do not bug the author about it unless you are still unable to get it working" );
        }

        if ( NativeCipher.load() )
        {
            logger.info( "Using OpenSSL based native cipher." );
        } else
        {
            logger.info( "Using standard Java JCE cipher. To enable the OpenSSL based native cipher, please make sure you are using 64 bit Ubuntu or Debian with libssl installed." );
        }
    }

    /**
     * Start this proxy instance by loading the configuration, plugins and
     * starting the connect thread.
     *
     * @throws Exception
     */
    @Override
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void start() throws Exception
    {
        System.setProperty( "java.net.preferIPv4Stack", "true" ); // Minecraft does not support IPv6
        System.setProperty( "io.netty.selectorAutoRebuildThreshold", "0" ); // Seems to cause Bungee to stop accepting connections
        ResourceLeakDetector.setEnabled( false ); // Eats performance

        eventLoops = PipelineUtils.newEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty IO Thread #%1$d" ).build() );

        File moduleDirectory = new File( "modules" );
        moduleManager.load( this, moduleDirectory );
        pluginManager.detectPlugins( moduleDirectory );

        pluginsFolder.mkdir();
        pluginManager.detectPlugins( pluginsFolder );

        pluginManager.loadPlugins();
        config.load();

        if (config.isForgeSupported()) 
        {
            // If we enable forge support, we need to register the FML FML|HS tag.
            registerChannel( ForgeConstants.FORGE_TAG );
            registerChannel( ForgeConstants.FORGE_HANDSHAKE_TAG );
            logger.info( "Forge support has been enabled." );
        }

        isRunning = true;

        pluginManager.enablePlugins();

        connectionThrottle = new ConnectionThrottle( config.getThrottle() );
        startListeners();

        saveThread.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                if ( getReconnectHandler() != null )
                {
                    getReconnectHandler().save();
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis( 5 ) );
        metricsThread.scheduleAtFixedRate( new Metrics(), 0, TimeUnit.MINUTES.toMillis( Metrics.PING_INTERVAL ) );
    }

    public void startListeners()
    {
        for ( final ListenerInfo info : config.getListeners() )
        {
            ChannelFutureListener listener = new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if ( future.isSuccess() )
                    {
                        listeners.add( future.channel() );
                        getLogger().log( Level.INFO, "Listening on {0}", info.getHost() );
                    } else
                    {
                        getLogger().log( Level.WARNING, "Could not bind to host " + info.getHost(), future.cause() );
                    }
                }
            };
            new ServerBootstrap()
                    .channel( PipelineUtils.getServerChannel() )
                    .option( ChannelOption.SO_REUSEADDR, true ) // TODO: Move this elsewhere!
                    .childAttr( PipelineUtils.LISTENER, info )
                    .childHandler( PipelineUtils.SERVER_CHILD )
                    .group( eventLoops )
                    .localAddress( info.getHost() )
                    .bind().addListener( listener );

            if ( info.isQueryEnabled() )
            {
                ChannelFutureListener bindListener = new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if ( future.isSuccess() )
                        {
                            listeners.add( future.channel() );
                            getLogger().log( Level.INFO, "Started query on {0}", future.channel().localAddress() );
                        } else
                        {
                            getLogger().log( Level.WARNING, "Could not bind to host " + info.getHost(), future.cause() );
                        }
                    }
                };
                new RemoteQuery( this, info ).start( PipelineUtils.getDatagramChannel(), new InetSocketAddress( info.getHost().getAddress(), info.getQueryPort() ), eventLoops, bindListener );
            }
        }
    }

    public void stopListeners()
    {
        for ( Channel listener : listeners )
        {
            getLogger().log( Level.INFO, "Closing listener {0}", listener );
            try
            {
                listener.close().syncUninterruptibly();
            } catch ( ChannelException ex )
            {
                getLogger().severe( "Could not close listen thread" );
            }
        }
        listeners.clear();
    }

    @Override
    public void stop()
    {
        new Thread( "Shutdown Thread" )
        {
            @Override
            @SuppressFBWarnings("DM_EXIT")
            @SuppressWarnings("TooBroadCatch")
            public void run()
            {
                BungeeCord.this.isRunning = false;

                stopListeners();
                getLogger().info( "Closing pending connections" );

                connectionLock.readLock().lock();
                try
                {
                    getLogger().log( Level.INFO, "Disconnecting {0} connections", connections.size() );
                    for ( UserConnection user : connections.values() )
                    {
                        user.disconnect( getTranslation( "restart" ) );
                    }
                } finally
                {
                    connectionLock.readLock().unlock();
                }

                getLogger().info( "Closing IO threads" );
                eventLoops.shutdownGracefully();
                try
                {
                    eventLoops.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
                } catch ( InterruptedException ex )
                {
                }

                if ( reconnectHandler != null )
                {
                    getLogger().info( "Saving reconnect locations" );
                    reconnectHandler.save();
                    reconnectHandler.close();
                }
                saveThread.cancel();
                metricsThread.cancel();

                // TODO: Fix this shit
                getLogger().info( "Disabling plugins" );
                for ( Plugin plugin : Lists.reverse( new ArrayList<>( pluginManager.getPlugins() ) ) )
                {
                    try
                    {
                        plugin.onDisable();
                        for ( Handler handler : plugin.getLogger().getHandlers() )
                        {
                            handler.close();
                        }
                    } catch ( Throwable t )
                    {
                        getLogger().log( Level.SEVERE, "Exception disabling plugin " + plugin.getDescription().getName(), t );
                    }
                    getScheduler().cancel( plugin );
                    plugin.getExecutorService().shutdownNow();
                }

                getLogger().info( "Thank you and goodbye" );
                // Need to close loggers after last message!
                for ( Handler handler : getLogger().getHandlers() )
                {
                    handler.close();
                }
                System.exit( 0 );
            }
        }.start();
    }

    /**
     * Broadcasts a packet to all clients that is connected to this instance.
     *
     * @param packet the packet to send
     */
    public void broadcast(DefinedPacket packet)
    {
        connectionLock.readLock().lock();
        try
        {
            for ( UserConnection con : connections.values() )
            {
                con.unsafe().sendPacket( packet );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public String getName()
    {
        return "BungeeCord";
    }

    @Override
    public String getVersion()
    {
        return ( BungeeCord.class.getPackage().getImplementationVersion() == null ) ? "unknown" : BungeeCord.class.getPackage().getImplementationVersion();
    }

    @Override
    public String getTranslation(String name, Object... args)
    {
        String translation = "<translation '" + name + "' missing>";
        try
        {
            translation = MessageFormat.format( bundle.getString( name ), args );
        } catch ( MissingResourceException ex )
        {
        }
        return translation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ProxiedPlayer> getPlayers()
    {
        connectionLock.readLock().lock();
        try
        {
            return Collections.unmodifiableCollection( new HashSet( connections.values() ) );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public int getOnlineCount()
    {
        return connections.size();
    }

    @Override
    public ProxiedPlayer getPlayer(String name)
    {
        connectionLock.readLock().lock();
        try
        {
            return connections.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    public UserConnection getPlayerByOfflineUUID(UUID name)
    {
        connectionLock.readLock().lock();
        try
        {
            return connectionsByOfflineUUID.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public ProxiedPlayer getPlayer(UUID uuid)
    {
        connectionLock.readLock().lock();
        try
        {
            for ( ProxiedPlayer proxiedPlayer : connections.values() )
            {
                if ( proxiedPlayer.getUniqueId().equals( uuid ) )
                {
                    return proxiedPlayer;
                }
            }

            return null;
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public Map<String, ServerInfo> getServers()
    {
        return config.getServers();
    }

    @Override
    public ServerInfo getServerInfo(String name)
    {
        return getServers().get( name );
    }

    @Override
    @Synchronized("pluginChannels")
    public void registerChannel(String channel)
    {
        pluginChannels.add( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public void unregisterChannel(String channel)
    {
        pluginChannels.remove( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public Collection<String> getChannels()
    {
        return Collections.unmodifiableCollection( pluginChannels );
    }

    public PluginMessage registerChannels()
    {
        return new PluginMessage( "REGISTER", Util.format( pluginChannels, "\00" ).getBytes( Charsets.UTF_8 ), false );
    }

    @Override
    public int getProtocolVersion()
    {
        return Protocol.supportedVersions.get( Protocol.supportedVersions.size() - 1 );
    }

    @Override
    public String getGameVersion()
    {
        return "1.7.9";
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address, String motd, boolean restricted, boolean modded)
    {
        return new BungeeServerInfo( name, address, motd, restricted, modded );
    }

    @Override
    public CommandSender getConsole()
    {
        return ConsoleCommandSender.getInstance();
    }

    @Override
    public void broadcast(String message)
    {
        broadcast( TextComponent.fromLegacyText( message ) );
    }

    @Override
    public void broadcast(BaseComponent... message)
    {
        getConsole().sendMessage( BaseComponent.toLegacyText( message ) );
        broadcast( new Chat( ComponentSerializer.toString( message ) ) );
    }

    @Override
    public void broadcast(BaseComponent message)
    {
        getConsole().sendMessage( message.toLegacyText() );
        broadcast( new Chat( ComponentSerializer.toString( message ) ) );
    }

    public void addConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.put( con.getName(), con );
            connectionsByOfflineUUID.put( con.getPendingConnection().getOfflineId(), con );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    public void removeConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.remove( con.getName() );
            connectionsByOfflineUUID.remove( con.getPendingConnection().getOfflineId() );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    public Collection<String> getDisabledCommands()
    {
        return config.getDisabledCommands();
    }

    @Override
    public Collection<ProxiedPlayer> matchPlayer(final String partialName)
    {
        Preconditions.checkNotNull( partialName, "partialName" );

        ProxiedPlayer exactMatch = getPlayer( partialName );
        if ( exactMatch != null )
        {
            return Collections.singleton( exactMatch );
        }

        return Sets.newHashSet( Iterables.find( getPlayers(), new Predicate<ProxiedPlayer>()
        {

            @Override
            public boolean apply(ProxiedPlayer input)
            {
                return ( input == null ) ? false : input.getName().toLowerCase().contains( partialName.toLowerCase() );
            }
        } ) );
    }
}
