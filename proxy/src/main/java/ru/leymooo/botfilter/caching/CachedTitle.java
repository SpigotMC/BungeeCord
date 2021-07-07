package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Subtitle;
import net.md_5.bungee.protocol.packet.Title;
import net.md_5.bungee.protocol.packet.TitleTimes;
import ru.leymooo.botfilter.config.Settings;

/**
 * @author Leymooo
 */
public class CachedTitle
{

    private ByteBuf[] times;
    private ByteBuf[] title = null;
    private ByteBuf[] subtitle = null;

    public CachedTitle(String raw, int in, int stay, int out)
    {
        if ( !raw.isEmpty() )
        {
            String[] titles = raw.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).split( "%nl%" );
            String title = titles[0];
            String subtitle = titles.length == 2 ? titles[1] : null;
            if ( !title.isEmpty() )
            {
                this.title = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];
                Title titlePacket = new Title();
                titlePacket.setAction( Title.Action.TITLE );
                titlePacket.setText( ComponentSerializer.toString( TextComponent.fromLegacyText( ChatColor.translateAlternateColorCodes( '&', title ) ) ) );
                PacketUtils.fillArray( this.title, titlePacket, Protocol.GAME );
            }
            if ( subtitle != null && !subtitle.isEmpty() )
            {
                this.subtitle = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];
                Title subTitlePacket = new Title();
                subTitlePacket.setAction( Title.Action.SUBTITLE );
                subTitlePacket.setText( ComponentSerializer.toString( TextComponent.fromLegacyText( ChatColor.translateAlternateColorCodes( '&', subtitle ) ) ) );
                PacketUtils.fillArray( this.subtitle, subTitlePacket, ProtocolConstants.MINECRAFT_1_8, ProtocolConstants.MINECRAFT_1_16_4, Protocol.GAME );
                Subtitle subtitlePacket1 = new Subtitle();
                subtitlePacket1.setText( subTitlePacket.getText() );
                PacketUtils.fillArray( this.subtitle, subtitlePacket1, ProtocolConstants.MINECRAFT_1_17, ProtocolConstants.getLastSupportedProtocol(), Protocol.GAME );
            }

            if ( this.title != null || this.subtitle != null )
            {
                this.times = new ByteBuf[PacketUtils.PROTOCOLS_COUNT];
                Title times = new Title();
                times.setFadeIn( in );
                times.setStay( stay );
                times.setFadeOut( out );
                times.setAction( Title.Action.TIMES );
                PacketUtils.fillArray( this.times, times, ProtocolConstants.MINECRAFT_1_8, ProtocolConstants.MINECRAFT_1_16_4, Protocol.GAME );
                TitleTimes times1 = new TitleTimes();
                times1.setFadeIn( in );
                times1.setStay( stay );
                times1.setFadeOut( out );
                PacketUtils.fillArray( this.times, times1, ProtocolConstants.MINECRAFT_1_17, ProtocolConstants.getLastSupportedProtocol(), Protocol.GAME );

            }
        }
    }

    public void writeTitle(Channel channel, int version)
    {
        version = PacketUtils.rewriteVersion( version );
        if ( title != null )
        {
            channel.write( title[version].retainedDuplicate() );
        }
        if ( subtitle != null )
        {
            channel.write( subtitle[version].retainedDuplicate() );
        }
        if ( times != null )
        {
            channel.write( times[version].retainedDuplicate() );
        }
    }

    public void test()
    {
        times[0].retainedDuplicate().release();
        title[0].retainedDuplicate().release();
        subtitle[0].retainedDuplicate().release();
    }

    public void release()
    {
        if ( title != null )
        {
            for ( ByteBuf buf : title )
            {
                PacketUtils.releaseByteBuf( buf );
            }
            title = null;
        }
        if ( subtitle != null )
        {
            for ( ByteBuf buf : subtitle )
            {
                PacketUtils.releaseByteBuf( buf );
            }
            subtitle = null;
        }
        if ( times != null )
        {
            for ( ByteBuf buf : times )
            {
                PacketUtils.releaseByteBuf( buf );
            }
            times = null;
        }
    }
}
