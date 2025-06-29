package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.nbt.limit.NBTLimiter;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CustomClickAction extends DefinedPacket
{

    private String id;
    private TypedTag data;

    @Override
    public void read(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        id = readString( buf );
        data = readLengthPrefixed( (buf0) -> (TypedTag) readTag( buf0, protocolVersion, new NBTLimiter( 32768L, 16 ) ), buf, 65536 );
    }

    @Override
    public void write(ByteBuf buf, Protocol protocol, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( id, buf );
        writeLengthPrefixed( data, (data0, buf0) -> writeTag( data0, buf0, protocolVersion ), buf, 65536 );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
