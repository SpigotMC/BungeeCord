package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Packet2Handshake extends DefinedPacket
{

    private byte protocolVersion;
    private String username;
    private String host;
    private int port;

    private Packet2Handshake()
    {
        super( 0x02 );
    }

    @Override
    public void read(ByteBuf buf)
    {
        protocolVersion = buf.readByte();
        username = readString( buf );
        host = readString( buf );
        port = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( protocolVersion );
        writeString( username, buf );
        writeString( host, buf );
        buf.writeInt( port );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
