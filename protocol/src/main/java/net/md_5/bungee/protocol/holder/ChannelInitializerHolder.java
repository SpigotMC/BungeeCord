package net.md_5.bungee.protocol.holder;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.Data;

/*
 * This class is used to hold a ChannelInitializer<Channel> object.
 * This could be very usefully for plugins like Geyser or ViaVersion
 * That require to access the ChannelInitializer to modify the pipeline
 *
 * THIS IS VERY UNSAFE AND SHOULD BE USED WITH CAUTION
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
