package net.md_5.bungee.packet;

import io.netty.channel.Channel;

public abstract class PacketHandler
{

    @Override
    public abstract String toString();

    public void connected(Channel channel) throws Exception
    {
    }

    public void disconnected(Channel channel) throws Exception
    {
    }

    public void exception(Throwable t) throws Exception
    {
    }

    public void handle(byte[] buf) throws Exception
    {
    }

    public void handle(Packet0KeepAlive alive) throws Exception
    {
    }

    public void handle(Packet1Login login) throws Exception
    {
    }

    public void handle(Packet2Handshake handshake) throws Exception
    {
    }

    public void handle(Packet3Chat chat) throws Exception
    {
    }

    public void handle(Packet9Respawn respawn) throws Exception
    {
    }

    public void handle(PacketC9PlayerListItem playerList) throws Exception
    {
    }

    public void handle(PacketCCSettings settings) throws Exception
    {
    }

    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
    }

    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
    }

    public void handle(PacketFCEncryptionResponse encryptResponse) throws Exception
    {
    }

    public void handle(PacketFDEncryptionRequest encryptRequest) throws Exception
    {
    }

    public void handle(PacketFEPing ping) throws Exception
    {
    }

    public void handle(PacketFFKick kick) throws Exception
    {
    }
}
