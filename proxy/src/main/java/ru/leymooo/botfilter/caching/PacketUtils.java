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
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
import ru.leymooo.botfilter.packets.ChunkPacket;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SpawnPosition;
import ru.leymooo.botfilter.packets.TimeUpdate;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.SetSlot;

/**
 *
 * @author Leymooo
 */
public class PacketUtils
{

    public static HashMap<Class<? extends DefinedPacket>, CachedPacket> singlePackets = new HashMap<>();
    private static HashMap<KickType, CachedPacket> kickMessagesGame = new HashMap<KickType, CachedPacket>();
    private static HashMap<KickType, CachedPacket> kickMessagesLogin = new HashMap<KickType, CachedPacket>();
    public static CachedPacket[] captchaFailedMessage = new CachedPacket[ 2 ];

    public static CachedCaptcha captchas = new CachedCaptcha();

    public static CachedPacket resetSlot = new CachedPacket( new SetSlot( 0, 36, -1, 0, 0 ), Protocol.BotFilter );

    public static CachedPacket checkMessage;
    public static CachedPacket captchaCheckMessage;
    public static CachedPacket checkSus;

    public static ByteBuf createPacket(DefinedPacket packet, int id, int protocol)
    {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        DefinedPacket.writeVarInt( id, buffer );
        packet.write( buffer, ProtocolConstants.Direction.TO_CLIENT, protocol );
        return buffer;
    }

    public static void init()
    {
        for ( CachedPacket packet : singlePackets.values() )
        {
            packet.release();
        }
        singlePackets.clear();
        for ( CachedPacket packet : kickMessagesGame.values() )
        {
            packet.release();
        }
        kickMessagesGame.clear();

        for ( CachedPacket packet : captchaFailedMessage )
        {
            if ( packet != null )
            {
                packet.release();
            }
        }

        if ( checkMessage != null )
        {
            checkMessage.release();
            captchaCheckMessage.release();
            checkSus.release();
        }

        DefinedPacket[] packets =
        {
            new Login( -1, (short) 2, 0, (short) 0, (short) 100, "flat", false ),
            new SpawnPosition( 1, 60, 1 ), new PlayerPositionAndLook( 7.00, 450, 7.00, -5f, 48f, 9876, false ),
            new TimeUpdate( 1, Settings.IMP.WORLD_TIME ), new KeepAlive( 9876 ),
            new ChunkPacket( 0, 0, new byte[ 63 ], false ), new PlayerAbilities( (byte) 6, 0f, 0f ),
            new SetSlot( 0, 36, 358, 1, 0 )
        };

        for ( DefinedPacket packet : packets )
        {
            singlePackets.put( packet.getClass(), new CachedPacket( packet, Protocol.BotFilter ) );
        }
        Protocol kickGame = Protocol.GAME;

        checkMessage = new CachedPacket( createMessagePacket( Settings.IMP.MESSGAGES.CHECKING ), kickGame );
        checkSus = new CachedPacket( createMessagePacket( Settings.IMP.MESSGAGES.SUCCESSFULLY ), kickGame );
        captchaCheckMessage = new CachedPacket( createMessagePacket( Settings.IMP.MESSGAGES.CHECKING_CAPTCHA ), kickGame );
        captchaFailedMessage[1] = new CachedPacket( createMessagePacket( Settings.IMP.MESSGAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "2" ).replaceFirst( "%s", "попытки" ) ), kickGame );
        captchaFailedMessage[0] = new CachedPacket( createMessagePacket( Settings.IMP.MESSGAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "1" ).replaceFirst( "%s", "попытка" ) ), kickGame );

        Protocol kickLogin = Protocol.LOGIN;

        kickMessagesGame.put( KickType.PING, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_BIG_PING ), kickGame ) );
        kickMessagesGame.put( KickType.NOTPLAYER, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_NOT_PLAYER ), kickGame ) );
        kickMessagesGame.put( KickType.PROXY, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_PROXY ), kickGame ) );
        kickMessagesGame.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_COUNTRY ), kickGame ) );
        kickMessagesLogin.put( KickType.PING, new CachedPacket( createKickPacket( String.join( "", Settings.IMP.SERVER_PING_CHECK.KICK_MESSAGE ) ), kickLogin ) );
        kickMessagesLogin.put( KickType.PROXY, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_PROXY ), kickLogin ) );
        kickMessagesLogin.put( KickType.MANYCHECKS, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_MANY_CHECKS ), kickLogin ) );
        kickMessagesLogin.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSGAGES.KICK_COUNTRY ), kickLogin ) );
        BungeeCord bungee = BungeeCord.getInstance();
        kickMessagesLogin.put( KickType.THROTTLE, new CachedPacket( createKickPacket( bungee.getTranslation( "join_throttle_kick", TimeUnit.MILLISECONDS.toSeconds( bungee.getConfig().getThrottle() ) ) ), kickLogin ) );

    }

    private static DefinedPacket createKickPacket(String message)
    {
        return new Kick( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes( '&',
                                message.replace( "%prefix%", Settings.IMP.MESSGAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ) );
    }

    private static DefinedPacket createMessagePacket(String message)
    {
        return new Chat( ComponentSerializer.toString(
                TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes( '&',
                                message.replace( "%prefix%", Settings.IMP.MESSGAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ), (byte) ChatMessageType.CHAT.ordinal() );
    }

    public static void spawnPlayer(Channel channel, int version, boolean disableFall)
    {
        channel.write( singlePackets.get( Login.class ).get( version ), channel.voidPromise() );
        channel.write( singlePackets.get( SpawnPosition.class ).get( version ), channel.voidPromise() );
        channel.write( singlePackets.get( ChunkPacket.class ).get( version ), channel.voidPromise() );
        channel.write( singlePackets.get( TimeUpdate.class ).get( version ), channel.voidPromise() );
        if ( disableFall )
        {
            channel.write( singlePackets.get( PlayerAbilities.class ).get( version ), channel.voidPromise() );
        }
        channel.write( singlePackets.get( PlayerPositionAndLook.class ).get( version ), channel.voidPromise() );
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
        PROXY,
        COUNTRY,
        THROTTLE,
        PING;
    }

}
