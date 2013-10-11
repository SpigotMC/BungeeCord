package net.md_5.bungee.protocol.handshake;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Packet0Handshake extends DefinedPacket
{

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int requestedProtocol;

    @Override
    public void read(ByteBuf buf)
    {
        protocolVersion = readVarInt( buf );
        serverAddress = readString( buf );
        serverPort = readVarInt( buf );
        requestedProtocol = readVarInt( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( protocolVersion, buf );
        writeString( serverAddress, buf );
        writeVarInt( serverPort, buf );
        writeVarInt( requestedProtocol, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
