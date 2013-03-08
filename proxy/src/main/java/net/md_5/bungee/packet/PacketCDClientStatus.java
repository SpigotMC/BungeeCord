package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCDClientStatus extends DefinedPacket
{

    public static PacketCDClientStatus CLIENT_LOGIN = new PacketCDClientStatus( (byte) 0 );

    /**
     * Sent from the client to the server upon respawn,
     *
     * @param payload 0 if initial spawn, 1 if respawn after death.
     */
    public PacketCDClientStatus(byte payload)
    {
        super( 0xCD );
        writeByte( payload );
    }

    public PacketCDClientStatus(ByteBuf buf)
    {
        super( 0xCD, buf );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
