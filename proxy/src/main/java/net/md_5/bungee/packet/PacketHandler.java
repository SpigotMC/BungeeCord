package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;

public abstract class PacketHandler
{

    private void nop(Object msg)
    {
        throw new UnsupportedOperationException( "No handler defined for packet " + msg.getClass() );
    }

    public void handle(ByteBuf buf) throws Exception
    {
        nop( buf );
    }

    public void handle(DefinedPacket packet) throws Exception
    {
        nop( packet );
    }

    public void handle(Packet0KeepAlive alive) throws Exception
    {
        nop( alive );
    }

    public void handle(Packet1Login login) throws Exception
    {
        nop( login );
    }

    public void handle(Packet2Handshake handshake) throws Exception
    {
        nop( handshake );
    }

    public void handle(Packet3Chat chat) throws Exception
    {
        nop( chat );
    }

    public void handle(Packet9Respawn respawn) throws Exception
    {
        nop( respawn );
    }

    public void handle(PacketC9PlayerListItem playerList) throws Exception
    {
        nop( playerList );
    }

    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
        nop( clientStatus );
    }

    public void handle(PacketFAPluginMessage pluginMessage) throws Exception
    {
        nop( pluginMessage );
    }

    public void handle(PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        nop( encryptResponse );
    }

    public void handle(PacketFDEncryptionRequest encryptRequest) throws Exception
    {
        nop( encryptRequest );
    }

    public void handle(PacketFEPing ping) throws Exception
    {
        nop( ping );
    }

    public void handle(PacketFFKick kick) throws Exception
    {
        nop( kick );
    }
}
