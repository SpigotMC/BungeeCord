package net.md_5.bungee.api.event;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Event;
import org.jetbrains.annotations.ApiStatus;

/*
 * This event is called for every minecraft connection related channel that is initialized by BungeeCord
 */

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@ApiStatus.Experimental
public class UnsafeChannelInitializeEvent extends Event
{
    /**
     * The raw netty channel that is being initialized
     */
    private Channel channel;
    /**
     * Channel decodes packets directed to server
     */
    private boolean server;
}
