package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Chat extends DefinedPacket
{

    private String message;
    private byte position;

    public Chat(String message)
    {
        this( message, (byte) 0 );
    }

    @Override
    public void read(ByteBuf buf, Protocol.ProtocolDirection direction, int protocolVersion)
    {
        message = readString( buf );
        if ( direction.toString().equals( "TO_CLIENT" ) && protocolVersion >= 7 )
        {
            position = buf.readByte();
        }
    }

    @Override
    public void write(ByteBuf buf, Protocol.ProtocolDirection direction, int protocolVersion)
    {
        writeString( message, buf );
        if ( direction.toString().equals( "TO_CLIENT" ) && protocolVersion >= 7 )
        {
            buf.writeByte( position );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
