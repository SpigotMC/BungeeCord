package net.md_5.bungee.protocol.holder;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.Data;

/*
 * Holder class for frontend and backend channel initializers. BungeeCord will set these values
 * on startup, and they can be used by third party plugins to modify the channel pipeline.
 *
 * Please note that this API is unsafe and doesn't provide any guarantees about the stability of the
 * channel pipeline. Use at your own risk.
 */
@Data
public class ChannelInitializerHolder
{
    public static ChannelInitializerHolder serverChildHolder = null;
    public static ChannelInitializerHolder backendConnectorHolder = null;

    public ChannelInitializerHolder(ChannelInitializer<Channel> channelInitializer)
    {
        Preconditions.checkNotNull( channelInitializer, "channelInitializer" );
        this.channelInitializer = channelInitializer;
    }

    private ChannelInitializer<Channel> channelInitializer;
}
