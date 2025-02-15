package net.md_5.bungee.protocol.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.Getter;
import lombok.Setter;

/**
 * This class hold a netty channel initializer that calls the given {@link ChannelAcceptor}.
 * Use {@link BungeeChannelInitializer#create(ChannelAcceptor)} to create a new instance.
 * <p>
 * Please note that this API is unsafe and doesn't provide any guarantees about
 * the stability of the channel pipeline or the API itself. Use at your own
 * risk.
 */
public abstract class BungeeChannelInitializer
{

    public abstract ChannelAcceptor getChannelAcceptor();

    public abstract ChannelInitializer<Channel> getChannelInitializer();

    /**
     * Replaces the existing {@link ChannelAcceptor}.
     *
     * @apiNote Please note that this API is unsafe. Use at your own risk.
     */
    public abstract void setChannelAcceptor(ChannelAcceptor acceptor);

    /**
     * Replaces the existing {@link ChannelInitializer}.
     *
     * @apiNote Please note that this API is unsafe. Use at your own risk.
     */
    public abstract void setChannelInitializer(ChannelInitializer<Channel> channelInitializer);

    /**
     * Creates a new instance of BungeeChannelInitializer
     *
     * @param acceptor the {@link ChannelAcceptor} that will accept the channel
     * and initializer the pipeline
     * @return {@link BungeeChannelInitializer} containing a cached
     * {@link ChannelInitializer} that will call the acceptor
     */
    public static BungeeChannelInitializer create(ChannelAcceptor acceptor)
    {
        return new BungeeChannelInitializer()
        {
            @Getter
            @Setter
            private ChannelAcceptor channelAcceptor = acceptor;

            @Getter // cache the ChannelInitializer
            @Setter
            private ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>()
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
}
