package net.md_5.bungee.netty;

public abstract class PacketHandler extends net.md_5.bungee.protocol.packet.AbstractPacketHandler
{

    @Override
    public abstract String toString();

    public void exception(Throwable t) throws Exception
    {
    }

    public void handle(PacketWrapper packet) throws Exception
    {
    }

    public void connected(ChannelWrapper channel) throws Exception
    {
    }

    public void disconnected(ChannelWrapper channel) throws Exception
    {
    }
}
