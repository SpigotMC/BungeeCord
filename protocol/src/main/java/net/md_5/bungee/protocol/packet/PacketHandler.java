package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.packet.Packet1Login;
import net.md_5.bungee.protocol.packet.Packet3Chat;
import net.md_5.bungee.protocol.packet.PacketD0DisplayScoreboard;
import net.md_5.bungee.protocol.packet.PacketFEPing;
import net.md_5.bungee.protocol.packet.PacketFFKick;
import net.md_5.bungee.protocol.packet.PacketC9PlayerListItem;
import net.md_5.bungee.protocol.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.packet.Packet2Handshake;
import net.md_5.bungee.protocol.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.protocol.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.protocol.packet.PacketCDClientStatus;
import net.md_5.bungee.protocol.packet.PacketCCSettings;
import net.md_5.bungee.protocol.packet.PacketCFScoreboardScore;
import net.md_5.bungee.protocol.packet.PacketCEScoreboardObjective;
import net.md_5.bungee.protocol.packet.Packet9Respawn;
import net.md_5.bungee.protocol.packet.PacketD1Team;
import net.md_5.bungee.protocol.packet.Packet0KeepAlive;
import net.md_5.bungee.netty.ChannelWrapper;

public abstract class PacketHandler
{

    @Override
    public abstract String toString();

    public void connected(ChannelWrapper channel) throws Exception
    {
    }

    public void disconnected(ChannelWrapper channel) throws Exception
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

    public void handle(PacketCEScoreboardObjective objective) throws Exception
    {
    }

    public void handle(PacketCFScoreboardScore score) throws Exception
    {
    }

    public void handle(PacketD0DisplayScoreboard displayScoreboard) throws Exception
    {
    }

    public void handle(PacketD1Team team) throws Exception
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
