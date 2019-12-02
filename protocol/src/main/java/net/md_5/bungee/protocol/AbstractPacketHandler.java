package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.ClientStatus;
import net.md_5.bungee.protocol.packet.Commands;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginPayloadRequest;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.Team;
import net.md_5.bungee.protocol.packet.Title;
import net.md_5.bungee.protocol.packet.ViewDistance;

public abstract class AbstractPacketHandler
{
    protected void handleGeneral(PacketWrapper<?> packet) throws Exception
    {
    }

    protected void handleGeneralNoEntity(PacketWrapper<?> packet) throws Exception
    {
    }

    public void handleLegacyPing(PacketWrapper<LegacyPing> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleTabCompleteResponse(PacketWrapper<TabCompleteResponse> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handlePing(PacketWrapper<PingPacket> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleStatusRequest(PacketWrapper<StatusRequest> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleStatusResponse(PacketWrapper<StatusResponse> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleHandshake(PacketWrapper<Handshake> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleKeepAlive(PacketWrapper<KeepAlive> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLogin(PacketWrapper<Login> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleChat(PacketWrapper<Chat> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleRespawn(PacketWrapper<Respawn> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLoginRequest(PacketWrapper<LoginRequest> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleClientSettings(PacketWrapper<ClientSettings> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleClientStatus(PacketWrapper<ClientStatus> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handlePlayerListItem(PacketWrapper<PlayerListItem> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handlePlayerListHeaderFooter(PacketWrapper<PlayerListHeaderFooter> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleTabCompleteRequest(PacketWrapper<TabCompleteRequest> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleScoreboardObjective(PacketWrapper<ScoreboardObjective> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleScoreboardScore(PacketWrapper<ScoreboardScore> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleEncryptionRequest(PacketWrapper<EncryptionRequest> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleScoreboardDisplay(PacketWrapper<ScoreboardDisplay> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleTeam(PacketWrapper<Team> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleTitle(PacketWrapper<Title> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handlePluginMessage(PacketWrapper<PluginMessage> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleKick(PacketWrapper<Kick> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleEncryptionResponse(PacketWrapper<EncryptionResponse> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLoginSuccess(PacketWrapper<LoginSuccess> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLegacyHandshake(PacketWrapper<LegacyHandshake> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleSetCompression(PacketWrapper<SetCompression> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleBossBar(PacketWrapper<BossBar> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLoginPayloadRequest(PacketWrapper<LoginPayloadRequest> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleLoginPayloadResponse(PacketWrapper<LoginPayloadResponse> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleEntityStatus(PacketWrapper<EntityStatus> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleCommands(PacketWrapper<Commands> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }

    public void handleViewDistance(PacketWrapper<ViewDistance> packet) throws Exception
    {
        handleGeneralNoEntity( packet );
    }
}
