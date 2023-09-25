package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PacketWrapper
{

    public final DefinedPacket packet;
    public final ByteBuf buf;
    public final Protocol protocol;
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
