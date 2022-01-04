package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * @author David (_Esel)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResourcePack extends DefinedPacket
{
    private String hash;
    private boolean success;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        boolean request = direction == ProtocolConstants.Direction.TO_CLIENT;
        if ( request )
        {
            readString( buf );
        }
        hash = readString( buf, 40 );
        if ( !request )
        {
            success = readVarInt( buf ) == 0;
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( direction != ProtocolConstants.Direction.TO_SERVER )
        {
            throw new IllegalStateException( "invalid direction" );
        }
        if ( !success )
        {
            throw new IllegalStateException( "only successful responses supported" );
        }
        writeString( hash, buf );
        writeVarInt( 0, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
