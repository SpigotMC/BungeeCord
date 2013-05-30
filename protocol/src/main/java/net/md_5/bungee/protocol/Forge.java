package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.packet.forge.Forge1Login;
import net.md_5.bungee.protocol.skip.PacketReader;

public class Forge extends Vanilla
{

    @Getter
    private static final Forge instance = new Forge();

    public Forge()
    {
        classes[0x01] = Forge1Login.class;
        skipper = new PacketReader( this ); // TODO: :(
    }

    @Override
    public DefinedPacket read(short packetId, ByteBuf buf)
    {
        int start = buf.readerIndex();
        DefinedPacket packet = read( packetId, buf, this );
        if ( buf.readerIndex() == start )
        {
            packet = super.read( packetId, buf );
        }

        return packet;
    }
}
