package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.ProtocolConstants.Direction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginRequest extends DefinedPacket
{

    public static final int EXPECTED_MAX_LENGTH = 1 + ( 32 * 4 ); //BotFilter

    private String data;
    private PlayerPublicKey publicKey;

    @Override
    public void read(ByteBuf buf, Direction direction, int protocolVersion)
    {
        DefinedPacket.doLengthSanityChecks( buf, this, direction, protocolVersion, 0, EXPECTED_MAX_LENGTH ); //BotFilter
        data = readString( buf, 32 ); //BotFilter read 32 characters instead of 15

        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            publicKey = readPublicKey( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( data, buf );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
        {
            writePublicKey( publicKey, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
