package net.md_5.bungee.netty;

import net.md_5.bungee.protocol.packet.DefinedPacket;

public class PacketWrapper
{

    DefinedPacket packet;
    byte[] buf;

    public PacketWrapper(DefinedPacket packet, byte[] buf)
    {
        this.packet = packet;
        this.buf = buf;
    }
}
