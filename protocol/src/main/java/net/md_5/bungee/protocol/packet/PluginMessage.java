package net.md_5.bungee.protocol.packet;

import com.google.common.base.Preconditions;
import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.MinecraftInput;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PluginMessage extends DefinedPacket
{

    private String tag;
    private byte[] data;

    /**
     * Allow this packet to be sent as an "extended" packet.
     */
    private boolean allowExtendedPacket = false;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        tag = readString( buf );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_8 )
        {
            data = readArrayLegacy( buf );
        } else
        {
            int maxSize = direction == ProtocolConstants.Direction.TO_SERVER ? Short.MAX_VALUE : 0x100000;
            Preconditions.checkArgument(buf.readableBytes() < maxSize);
            data = new byte[ buf.readableBytes() ];
            buf.readBytes( data );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( tag, buf );
        if ( protocolVersion < ProtocolConstants.MINECRAFT_1_8 )
        {
            writeArrayLegacy( data, buf, allowExtendedPacket );
        } else
        {
            buf.writeBytes( data );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public DataInput getStream()
    {
        return new DataInputStream( new ByteArrayInputStream( data ) );
    }

    public MinecraftInput getMCStream()
    {
        return new MinecraftInput( Unpooled.wrappedBuffer( data ) );
    }
}
