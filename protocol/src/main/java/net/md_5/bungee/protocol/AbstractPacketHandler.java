package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.ClientStatus;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.Team;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.Title;

public abstract class AbstractPacketHandler
{

    public void handle(LegacyPing ping) throws Exception
    {
    }

    public void handle(TabCompleteResponse tabResponse) throws Exception
    {
    }

    public void handle(PingPacket ping) throws Exception
    {
    }

    public void handle(StatusRequest statusRequest) throws Exception
    {
    }

    public void handle(StatusResponse statusResponse) throws Exception
    {
    }

    public void handle(Handshake handshake) throws Exception
    {
    }

    public void handle(KeepAlive keepAlive) throws Exception
    {
    }

    public void handle(Login login) throws Exception
    {
    }

    public void handle(Chat chat) throws Exception
    {
    }

    public void handle(Respawn respawn) throws Exception
    {
    }

    public void handle(LoginRequest loginRequest) throws Exception
    {
    }

    public void handle(ClientSettings settings) throws Exception
    {
    }

    public void handle(ClientStatus clientStatus) throws Exception
    {
    }

    public void handle(PlayerListItem playerListItem) throws Exception
    {
    }

    public void handle(PlayerListHeaderFooter playerListHeaderFooter) throws Exception
    {
    }

    public void handle(TabCompleteRequest tabComplete) throws Exception
    {
    }

    public void handle(ScoreboardObjective scoreboardObjective) throws Exception
    {
    }

    public void handle(ScoreboardScore scoreboardScore) throws Exception
    {
    }

    public void handle(EncryptionRequest encryptionRequest) throws Exception
    {
    }

    public void handle(ScoreboardDisplay displayScoreboard) throws Exception
    {
    }

    public void handle(Team team) throws Exception
    {
    }

    public void handle(Title title) throws Exception
    {
    }

    public void handle(PluginMessage pluginMessage) throws Exception
    {
    }

    public void handle(Kick kick) throws Exception
    {
    }

    public void handle(EncryptionResponse encryptionResponse) throws Exception
    {
    }

    public void handle(LoginSuccess loginSuccess) throws Exception
    {
    }

    public void handle(LegacyHandshake legacyHandshake) throws Exception
    {
    }

    public void handle(SetCompression setCompression) throws Exception
    {
    }
}
