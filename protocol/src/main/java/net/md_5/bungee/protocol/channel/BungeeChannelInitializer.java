package net.md_5.bungee.protocol.channel;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * This class holds all minecraft related channel initializers. BungeeCord will set these values
 * on startup, and they can be used by third party plugins to modify the channel pipeline.
 *
 * Please note that this API is unsafe and doesn't provide any guarantees about the stability of the
 * channel pipeline. Use at your own risk.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class BungeeChannelInitializer
{
    /**
     * Holds the channel initializer for incoming connections
     */
    @Getter
    private static BungeeChannelInitializer frontendHolder;
    /**
     * Holds the channel initializer for the connection to the backend server
     */
    @Getter
    private static BungeeChannelInitializer backendHolder;
    /**
     * Holds the channel initializer for server info requests to the backend server
     */
    @Getter
    private static BungeeChannelInitializer serverInfoHolder;

    public abstract ChannelAcceptor getChannelAcceptor();
    public abstract ChannelInitializer<Channel> getChannelInitializer();

    /**
     * Creates a new instance of BungeeChannelInitializer
     *
     * @param acceptor the {@link ChannelAcceptor} that will accept the channel and initializer the pipeline
     * @return {@link BungeeChannelInitializer} containing a cached {@link ChannelInitializer} that will call the acceptor
     */
    public static BungeeChannelInitializer create(ChannelAcceptor acceptor)
    {
        return new BungeeChannelInitializer()
        {
            @Getter
            private final ChannelAcceptor channelAcceptor = acceptor;

            @Getter // cache the ChannelInitializer
            private final ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>()
            {
                @Override
                protected void initChannel(Channel channel) throws Exception
                {
                    if ( !getChannelAcceptor().accept( channel ) )
                    {
                        channel.close();
                    }
                }
            };
        };
    }

    public static void setFrontendHolder(BungeeChannelInitializer channelInitializer)
    {
        Preconditions.checkNotNull( channelInitializer, "channelInitializer" );
        frontendHolder = channelInitializer;
    }

    public static void setBackendHolder(BungeeChannelInitializer channelInitializer)
    {
        Preconditions.checkNotNull( channelInitializer, "channelInitializer" );
        backendHolder = channelInitializer;
    }

    public static void setServerInfoHolder(BungeeChannelInitializer channelInitializer)
    {
        Preconditions.checkNotNull( channelInitializer, "channelInitializer" );
        serverInfoHolder = channelInitializer;
    }
}
