package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class PacketWrapper<P extends DefinedPacket>
{

    public final P packet;
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
