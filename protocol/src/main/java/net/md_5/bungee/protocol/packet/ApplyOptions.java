package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApplyOptions extends DefinedPacket
{

    /**
     * The connection should be encrypted with the previously exchanged key.
     */
    public static final byte FLAG_ENCRYPTION = 0x1;
    /**
     * The connection should be compressed with the previously sent threshold.
     */
    public static final byte FLAG_COMPRESSION = 0x2;
    //
    private byte options;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        options = buf.readByte();
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        buf.writeByte( options );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public static boolean isEncryption(byte options)
    {
        return ( options & FLAG_ENCRYPTION ) != 0;
    }

    public static boolean isCompression(byte options)
    {
        return ( options & FLAG_COMPRESSION ) != 0;
    }
}
