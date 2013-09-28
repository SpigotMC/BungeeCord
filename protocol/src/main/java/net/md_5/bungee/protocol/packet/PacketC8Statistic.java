package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketC8Statistic extends DefinedPacket
{

    public PacketC8Statistic()
    {
        super( 0xC8 );
    }

    @Override
    public void read(ByteBuf buf)
    {
        int len = buf.readInt();
        for ( int i = 0; i < len; i++ )
        {
            readString( buf );
            buf.readInt();
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
