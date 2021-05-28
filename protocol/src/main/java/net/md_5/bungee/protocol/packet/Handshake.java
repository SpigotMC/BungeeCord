package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants.Direction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends DefinedPacket
{

    //BotFilter - see https://github.com/PaperMC/Waterfall/blob/master/BungeeCord-Patches/0060-Additional-DoS-mitigations.patch
    public static final int EXPECTED_MAX_LENGTH = 5 + ( ( 255 * 4 + 3 ) + 5 ) + 2 + 1;
    public static final int EXPECTED_MIN_LENGTH = 1 + 1 + 2 + 1;
    //BotFilter end

    private int protocolVersion;
    private String host;
    private int port;
    private int requestedProtocol;

    @Override
    public void read(ByteBuf buf)
    {
        DefinedPacket.doLengthSanityChecks( buf, this, Direction.TO_SERVER, -1, EXPECTED_MIN_LENGTH, EXPECTED_MAX_LENGTH ); //BotFilter
        protocolVersion = readVarInt( buf );
        host = readString( buf, 255 );
        port = buf.readUnsignedShort();
        requestedProtocol = readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( protocolVersion, buf );
        writeString( host, buf );
        buf.writeShort( port );
        writeVarInt( requestedProtocol, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
