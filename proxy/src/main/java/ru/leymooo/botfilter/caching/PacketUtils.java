package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Protocol.DirectionData;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
import ru.leymooo.botfilter.packets.ChunkPacket;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.TimeUpdate;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.SetExp;
import ru.leymooo.botfilter.packets.SetSlot;

/**
 *
 * @author Leymooo
 */
public class PacketUtils
{

    public static int PROTOCOLS_NUM = 11;
    
    /**
     * 0 - Login, 1 - SpawnPosition(null), 2 - ChunkData, 3 - TimeUpdate, 4 -
     * PlayerAbilities, 5 - PlayerPosAndLook, 6 - SetSlot(Map), 7 -
     * SetSlot(Reset), 8 - KeepAlive, 9 - 10 - CaptchaFailedMessage, 11 -
     * checkMessage, 12 - captchaCheckMessage, 13 - CheckSus, 14 -
     * PlayerPosAndLook, 15 - SetExp(reset)
     */
    public static CachedPacket[] packets = new CachedPacket[ 16 ];
    private static HashMap<KickType, CachedPacket> kickMessagesGame = new HashMap<KickType, CachedPacket>();
    private static HashMap<KickType, CachedPacket> kickMessagesLogin = new HashMap<KickType, CachedPacket>();

    public static CachedCaptcha captchas = new CachedCaptcha();

    public static CachedExpPacket expPacket;

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
        if ( expPacket != null )
        {
            expPacket.release();
        }
        for ( CachedPacket packet : packets )
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

        expPacket = new CachedExpPacket();

        titles[0] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE, 5, 90, 15 );
        titles[1] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_CAPTCHA, 5, 15, 10 );
        titles[2] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_SUS, 5, 20, 10 );

        DefinedPacket[] packets =
        {
            new Login( 0, (short) 2, 0, (short) 0, (short) 100, "flat", false ), //0
            null, //1
            new ChunkPacket( 0, 0, new byte[ 256 ], false ), //2
            new TimeUpdate( 1, Settings.IMP.TIME ), //3
            new PlayerAbilities( (byte) 6, 0f, 0f ), //4
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 40f, 9876, false ), //5
            new SetSlot( 0, 36, 358, 1, 0 ), //6
            new SetSlot( 0, 36, -1, 0, 0 ), //7
            new KeepAlive( 9876 ), //8
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "2" ).replaceFirst( "%s", "попытки" ) ), //9
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "1" ).replaceFirst( "%s", "попытка" ) ), //10
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING ), //11
            createMessagePacket( Settings.IMP.MESSAGES.CHECKING_CAPTCHA ), //12
            createMessagePacket( Settings.IMP.MESSAGES.SUCCESSFULLY ), //13
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 10f, 9876, false ), //14
            new SetExp( 0, 0, 0 ), //15

        };

        for ( int i = 0; i < packets.length; i++ )
        {
            PacketUtils.packets[i] = new CachedPacket( packets[i], Protocol.BotFilter, Protocol.GAME );
        }
        Protocol kickGame = Protocol.GAME;
        Protocol kickLogin = Protocol.LOGIN;

        kickMessagesGame.put( KickType.PING, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_BIG_PING ), kickGame ) );
        kickMessagesGame.put( KickType.NOTPLAYER, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_NOT_PLAYER ), kickGame ) );
        kickMessagesGame.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickGame ) );
        kickMessagesLogin.put( KickType.PING, new CachedPacket( createKickPacket( String.join( "", Settings.IMP.SERVER_PING_CHECK.KICK_MESSAGE ) ), kickLogin ) );
        kickMessagesLogin.put( KickType.MANYCHECKS, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_MANY_CHECKS ), kickLogin ) );
        kickMessagesLogin.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickLogin ) );
        BungeeCord bungee = BungeeCord.getInstance();
        kickMessagesLogin.put( KickType.THROTTLE, new CachedPacket( createKickPacket( bungee.getTranslation( "join_throttle_kick", TimeUnit.MILLISECONDS.toSeconds( bungee.getConfig().getThrottle() ) ) ), kickLogin ) );

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
        return new Chat( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes( '&',
                                message.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ), (byte) ChatMessageType.CHAT.ordinal() );
    }

    public static int getPacketId(DefinedPacket packet, DirectionData def, DirectionData prot, int version)
    {
        try
        {
            return def.getId( packet.getClass(), version );
        } catch ( Exception e )
        {
            return prot.getId( packet.getClass(), version );
        }
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

    public static void fillArray(ByteBuf[] buffer, DefinedPacket packet, Protocol protocol, Protocol secondProtocol)
    {
        if ( packet == null )
        {
            return;
        }
        Protocol.DirectionData prot = protocol.TO_CLIENT;
        Protocol.DirectionData prot2 = secondProtocol == null ? null : secondProtocol.TO_CLIENT;
        int oldPacketId = -1;
        ByteBuf oldBuf = null;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            int versionRewrited = rewriteVersion( version );
            int newPacketId = PacketUtils.getPacketId( packet, prot, prot2, version );
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
            default:
                throw new IllegalArgumentException( "Version is not supported" );
        }
    }

    public static void spawnPlayer(Channel channel, int version, boolean disableFall, boolean captcha)
    {
        channel.write( packets[0].get( version ), channel.voidPromise() ); //Login
        channel.write( packets[2].get( version ), channel.voidPromise() ); //ChunkData
        if ( disableFall )
        {
            channel.write( packets[4].get( version ), channel.voidPromise() ); //PlayerAbilities
        }
        if ( captcha )
        {
            channel.write( packets[5].get( version ), channel.voidPromise() ); //PlayerPosAndLook
        } else
        {
            channel.write( packets[14].get( version ), channel.voidPromise() ); //PlayerPosAndLook

        }
        channel.write( packets[3].get( version ), channel.voidPromise() ); //TimeUpdate
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
        wrapper.delayedClose( null );
    }

    public static enum KickType
    {
        MANYCHECKS,
        NOTPLAYER,
        COUNTRY,
        THROTTLE,
        PING;
    }

}
