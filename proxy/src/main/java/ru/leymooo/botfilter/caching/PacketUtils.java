package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Random;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.EmptyChunkPacket;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetExp;
import ru.leymooo.botfilter.packets.SetSlot;
import ru.leymooo.botfilter.packets.TimeUpdate;

/**
 * @author Leymooo
 */
public class PacketUtils
{

    public static int PROTOCOLS_COUNT = ProtocolConstants.SUPPORTED_VERSION_IDS.size();

    public static int CLIENTID = new Random().nextInt( Integer.MAX_VALUE - 100 ) + 50;

    private static final CachedPacket[] cachedPackets = new CachedPacket[ 16 ];
    private static final HashMap<KickType, CachedPacket> kickMessagesGame = new HashMap<KickType, CachedPacket>( 3 );
    private static final HashMap<KickType, CachedPacket> kickMessagesLogin = new HashMap<KickType, CachedPacket>( 4 );

    public static final CachedCaptcha captchas = new CachedCaptcha();

    public static CachedExpPackets expPackets;

    /**
     * 0 - Checking_fall, 1 - checking_captcha, 2 - sus
     */
    public static CachedTitle[] titles = new CachedTitle[ 3 ];

    public static ByteBuf createPacket(DefinedPacket packet, int id, int protocol)
    {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        DefinedPacket.writeVarInt( id, buffer );
        packet.write( buffer, ProtocolConstants.Direction.TO_CLIENT, protocol );
        return buffer;
    }

    public static void init()
    {
        if ( expPackets != null )
        {
            expPackets.release();
        }
        for ( CachedPacket packet : cachedPackets )
        {
            if ( packet != null )
            {
                packet.release();
            }
        }
        for ( CachedTitle title : titles )
        {
            if ( title != null )
            {
                title.release();
            }
        }
        for ( CachedPacket packet : kickMessagesGame.values() )
        {
            packet.release();
        }
        kickMessagesGame.clear();

        expPackets = new CachedExpPackets();

        titles[0] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE, 5, 90, 15 );
        titles[1] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_CAPTCHA, 5, 15, 10 );
        titles[2] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_SUS, 5, 20, 10 );

        DefinedPacket[] packets =
        {
            new Login( CLIENTID, (short) 2, 0, 1, (short) 0, (short) 100, "flat", 2, false, true ), //0
            new EmptyChunkPacket( 0, 0 ), //1
            new TimeUpdate( 1, 23700 ), //2
            new PlayerAbilities( (byte) 6, 0f, 0f ), //3
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 38f, 9876, false ), //4
            new SetSlot( 0, 36, 358, 1, 0 ), //5 map 1.8+
            new SetSlot( 0, 36, -1, 0, 0 ), //6 map reset
            new KeepAlive( 9876 ), //7
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "2" ).replaceFirst( "%s", "попытки" ) ), //8
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "1" ).replaceFirst( "%s", "попытка" ) ), //9
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING ), //10
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA ), //11
            createMessagePacket( Settings.IMP.MESSAGES.SUCCESSFULLY ), //12
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 10f, 9876, false ), //13
            new SetExp( 0, 0, 0 ), //14
            createPluginMessage(), //15
        };

        for ( int i = 0; i < packets.length; i++ )
        {
            PacketUtils.cachedPackets[i] = new CachedPacket( packets[i], Protocol.BotFilter, Protocol.GAME );
        }
        Protocol kickGame = Protocol.GAME;
        Protocol kickLogin = Protocol.LOGIN;

        kickMessagesGame.put( KickType.PING, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_BIG_PING ), kickGame ) );
        kickMessagesGame.put( KickType.NOTPLAYER, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_NOT_PLAYER ), kickGame ) );
        kickMessagesGame.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickGame ) );
        kickMessagesLogin.put( KickType.PING, new CachedPacket( createKickPacket( String.join( "", Settings.IMP.SERVER_PING_CHECK.KICK_MESSAGE ) ), kickLogin ) );
        kickMessagesLogin.put( KickType.MANYCHECKS, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_MANY_CHECKS ), kickLogin ) );
        kickMessagesLogin.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickLogin ) );
    }

    private static DefinedPacket createKickPacket(String message)
    {
        return new Kick( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes( '&',
                                message.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ) );
    }

    private static DefinedPacket createMessagePacket(String message)
    {
        if ( message.isEmpty() )
        {
            return null;
        }
        return new Chat( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes( '&',
                                message.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ), (byte) ChatMessageType.CHAT.ordinal() );
    }

    private static DefinedPacket createPluginMessage()
    {
        ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();
        DefinedPacket.writeString( "BotFilter (https://vk.cc/8hr1pU)", brand );
        DefinedPacket packet = new PluginMessage( "MC|Brand", DefinedPacket.toArray( brand ), false );
        brand.release();
        return packet;
    }

    public static int getPacketId(DefinedPacket packet, int version, Protocol... protocols)
    {
        for ( Protocol protocol : protocols )
        {
            try
            {
                return protocol.TO_CLIENT.getId( packet.getClass(), version );
            } catch ( Exception ignore )
            {
            }
        }

        throw new IllegalStateException( "Can not get id for " + packet.getClass().getSimpleName() + "(" + version + ")" );
    }

    public static void releaseByteBuf(ByteBuf buf)
    {
        if ( buf != null && buf.refCnt() != 0 )
        {
            while ( buf.refCnt() != 0 )
            {
                buf.release();
            }
        }
    }

    public static void fillArray(ByteBuf[] buffer, DefinedPacket packet, Protocol... protocols)
    {
        if ( packet == null )
        {
            return;
        }
        int oldPacketId = -1;
        ByteBuf oldBuf = null;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            int versionRewrited = rewriteVersion( version );
            int newPacketId = PacketUtils.getPacketId( packet, version, protocols );
            if ( newPacketId != oldPacketId )
            {
                oldPacketId = newPacketId;
                oldBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                buffer[versionRewrited] = oldBuf;
            } else
            {
                ByteBuf newBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                if ( newBuf.equals( oldBuf ) )
                {
                    buffer[versionRewrited] = oldBuf;
                    newBuf.release();
                } else
                {
                    oldBuf = newBuf;
                    buffer[versionRewrited] = oldBuf;
                }
            }
        }
    }

    public static int rewriteVersion(int version)
    {
        switch ( version )
        {
            case ProtocolConstants.MINECRAFT_1_8:
                return 0;
            case ProtocolConstants.MINECRAFT_1_9:
                return 1;
            case ProtocolConstants.MINECRAFT_1_9_1:
                return 2;
            case ProtocolConstants.MINECRAFT_1_9_2:
                return 3;
            case ProtocolConstants.MINECRAFT_1_9_4:
                return 4;
            case ProtocolConstants.MINECRAFT_1_10:
                return 5;
            case ProtocolConstants.MINECRAFT_1_11:
                return 6;
            case ProtocolConstants.MINECRAFT_1_11_1:
                return 7;
            case ProtocolConstants.MINECRAFT_1_12:
                return 8;
            case ProtocolConstants.MINECRAFT_1_12_1:
                return 9;
            case ProtocolConstants.MINECRAFT_1_12_2:
                return 10;
            case ProtocolConstants.MINECRAFT_1_13:
                return 11;
            case ProtocolConstants.MINECRAFT_1_13_1:
                return 12;
            case ProtocolConstants.MINECRAFT_1_13_2:
                return 13;
            case ProtocolConstants.MINECRAFT_1_14:
                return 14;
            case ProtocolConstants.MINECRAFT_1_14_1:
                return 15;
            case ProtocolConstants.MINECRAFT_1_14_2:
                return 16;
            case ProtocolConstants.MINECRAFT_1_14_3:
                return 17;
            case ProtocolConstants.MINECRAFT_1_14_4:
                return 18;
            case ProtocolConstants.MINECRAFT_1_15:
                return 19;
            case ProtocolConstants.MINECRAFT_1_15_1:
                return 20;
            case ProtocolConstants.MINECRAFT_1_15_2:
                return 21;
            default:
                throw new IllegalArgumentException( "Version is not supported" );
        }
    }

    public static void spawnPlayer(Channel channel, int version, boolean disableFall, boolean captcha)
    {
        channel.write( getCachedPacket( PacketsPosition.LOGIN ).get( version ), channel.voidPromise() );
        channel.write( getCachedPacket( PacketsPosition.PLUGIN_MESSAGE ).get( version ), channel.voidPromise() );
        channel.write( getCachedPacket( PacketsPosition.CHUNK ).get( version ), channel.voidPromise() );
        if ( disableFall )
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERABILITIES ).get( version ), channel.voidPromise() );
        }
        if ( captcha )
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERPOSANDLOOK_CAPTCHA ).get( version ), channel.voidPromise() );
        } else
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERPOSANDLOOK ).get( version ), channel.voidPromise() );

        }
        channel.write( getCachedPacket( PacketsPosition.TIME ).get( version ), channel.voidPromise() );
        //channel.flush(); Не очищяем поскольку это будет в другом месте
    }

    public static void kickPlayer(KickType kick, Protocol protocol, ChannelWrapper wrapper, int version)
    {
        if ( wrapper.isClosed() || wrapper.isClosing() )
        {
            return;
        }
        if ( protocol == Protocol.GAME )
        {
            wrapper.write( kickMessagesGame.get( kick ).get( version ) );
        } else
        {
            wrapper.write( kickMessagesLogin.get( kick ).get( version ) );
        }
        wrapper.close();
    }

    public static CachedPacket getCachedPacket(int pos)
    {
        return cachedPackets[pos];
    }

    public static enum KickType
    {
        MANYCHECKS,
        NOTPLAYER,
        COUNTRY,
        // THROTTLE,
        PING;
    }

}
