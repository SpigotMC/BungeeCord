package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.ProtocolConstants;
import ru.leymooo.botfilter.packets.MapDataPacket;

/**
 * @author Leymooo
 */
public class CachedCaptcha
{

    //уже пора с этим чтото придумать
    private static final int PACKETID_18 = 52;
    private static final int PACKETID_19 = 36;
    private static final int PACKETID_113 = 38;
    private static final int PACKETID_114and116 = 38;
    private static final int PACKETID_115 = 39;
    private static final Random random = new Random();

    private static final CaptchaHolder[] captchas = new CaptchaHolder[900];
    private static final AtomicInteger counter = new AtomicInteger();

    public static boolean generated = false;

    public void createCaptchaPacket(MapDataPacket map, String answer)
    {

        ByteBuf byteBuf18 = PacketUtils.createPacket( map, PACKETID_18, ProtocolConstants.MINECRAFT_1_8 );
        ByteBuf byteBuf19 = PacketUtils.createPacket( map, PACKETID_19, ProtocolConstants.MINECRAFT_1_9 );
        ByteBuf byteBuf113 = PacketUtils.createPacket( map, PACKETID_113, ProtocolConstants.MINECRAFT_1_13 );
        ByteBuf byteBuf114And116 = PacketUtils.createPacket( map, PACKETID_114and116, ProtocolConstants.MINECRAFT_1_14 );
        ByteBuf byteBuf115 = PacketUtils.createPacket( map, PACKETID_115, ProtocolConstants.MINECRAFT_1_15 );

        captchas[counter.getAndIncrement()] = new CaptchaHolder( answer, byteBuf18, byteBuf19, byteBuf113, byteBuf114And116, byteBuf115 );

        //TODO: Do something with this shit.
    }

    public CaptchaHolder randomCaptcha()
    {
        return captchas[random.nextInt( captchas.length )];
    }

    public void checkNotNull()
    {
        for ( int i = 0; i < captchas.length; i++ )
        {
            if ( captchas[i] == null )
            {
                throw new RuntimeException( "index i is null" );
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class CaptchaHolder
    {
        private final String answer;
        private final ByteBuf buf18, buf19, buf113, buf114And116, buf115;

        public void write(Channel channel, int version, boolean flush)
        {
            if ( version == ProtocolConstants.MINECRAFT_1_8 )
            {
                channel.write( buf18.retainedDuplicate(), channel.voidPromise() );
            } else if ( version < ProtocolConstants.MINECRAFT_1_13 )
            {
                channel.write( buf19.retainedDuplicate(), channel.voidPromise() );
            } else if ( version < ProtocolConstants.MINECRAFT_1_14 )
            {
                channel.write( buf113.retainedDuplicate(), channel.voidPromise() );
            } else if ( version < ProtocolConstants.MINECRAFT_1_15 )
            {
                channel.write( buf114And116.retainedDuplicate(), channel.voidPromise() );
            } else if ( version < ProtocolConstants.MINECRAFT_1_16 )
            {
                channel.write( buf115.retainedDuplicate(), channel.voidPromise() );
            } else
            {
                channel.write( buf114And116.retainedDuplicate(), channel.voidPromise() );
            }
            if ( flush )
            {
                channel.flush();
            }
        }
    }
}
