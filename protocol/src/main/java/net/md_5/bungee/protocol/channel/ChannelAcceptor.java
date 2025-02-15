package net.md_5.bungee.protocol.channel;

import io.netty.channel.Channel;

@FunctionalInterface
public interface ChannelAcceptor
{

    /**
     * Inside this method the pipeline should be initialized.
     *
     * @param channel the channel to be accepted and initialized
     * @return if the channel was accepted
     */
    boolean accept(Channel channel);
}
