package net.md_5.bungee.packet;

public abstract class PacketHandler
{

    public void handle(DefinedPacket packet) throws Exception
    {
        throw new UnsupportedOperationException( "No handler defined for packet " + packet.getClass() );
    }

    public void handle(Packet1Login login) throws Exception
    {
    }

    public void handle(Packet2Handshake handshake) throws Exception
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
}
