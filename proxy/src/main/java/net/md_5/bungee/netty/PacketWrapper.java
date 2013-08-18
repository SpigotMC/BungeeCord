package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.packet.DefinedPacket;

@RequiredArgsConstructor
public class PacketWrapper
{

    public final DefinedPacket packet;
    public final ByteBuf buf;
    @Setter
    private boolean released;

    public void trySingleRelease()
    {
        if ( !released )
        {
            buf.release();
            released = true;
        }
    }
}
