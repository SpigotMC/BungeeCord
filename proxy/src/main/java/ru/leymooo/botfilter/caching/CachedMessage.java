package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.SystemChat;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Leymooo
 */
public class CachedMessage
{

    private ByteBuf[] bufs = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];

    public CachedMessage(String message)
    {
        if ( message != null && !message.isEmpty() )
        {
            PacketUtils.fillArray( this.bufs, createMessagePacket( message, false ), ProtocolConstants.MINECRAFT_1_8, ProtocolConstants.MINECRAFT_1_18_2, Protocol.GAME );
            PacketUtils.fillArray( this.bufs, createMessagePacket( message, true ), ProtocolConstants.MINECRAFT_1_19, ProtocolConstants.getLastSupportedProtocol(), Protocol.GAME );
        }
    }


    private static DefinedPacket createMessagePacket(String message, boolean is119)
    {
        message = ComponentSerializer.toString(
            TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes( '&',
                    message.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) );


        if ( is119 )
        {
            return new SystemChat( message, ChatMessageType.SYSTEM.ordinal() );
        } else
        {
            return new Chat( message, (byte) ChatMessageType.CHAT.ordinal() );

        }

    }

    public void write(Channel channel, int version)
    {
        ByteBuf buf = bufs[PacketUtils.rewriteVersion( version )];
        if ( buf != null )
        {
            channel.write( buf.retainedDuplicate() );
        }
    }

    public void release()
    {
        for ( ByteBuf buf : bufs )
        {
            PacketUtils.releaseByteBuf( buf );
        }
    }
}
