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
public class LoginRequest extends DefinedPacket
{

    public static final int EXPECTED_MAX_LENGTH = 1 + ( 32 * 4 ); //BotFilter

    private String data;

    @Override
    public void read(ByteBuf buf, Direction direction, int protocolVersion)
    {
        DefinedPacket.doLengthSanityChecks( buf, this, direction, protocolVersion, 0, EXPECTED_MAX_LENGTH ); //BotFilter
        data = readString( buf, 32 ); //BotFilter read 32 characters instead of 15
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( data, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
