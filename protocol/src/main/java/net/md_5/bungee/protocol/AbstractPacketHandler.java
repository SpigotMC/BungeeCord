package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.packet.BossBar;
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

    public boolean handle(LegacyPing ping) throws Exception
    {
    	return false;
    }

    public boolean handle(TabCompleteResponse tabResponse) throws Exception
    {
    	return false;
    }

    public boolean handle(PingPacket ping) throws Exception
    {
    	return false;
    }

    public boolean handle(StatusRequest statusRequest) throws Exception
    {
    	return false;
    }

    public boolean handle(StatusResponse statusResponse) throws Exception
    {
    	return false;
    }

    public boolean handle(Handshake handshake) throws Exception
    {
    	return false;
    }

    public boolean handle(KeepAlive keepAlive) throws Exception
    {
    	return false;
    }

    public boolean handle(Login login) throws Exception
    {
    	return false;
    }

    public boolean handle(Chat chat) throws Exception
    {
    	return false;
    }

    public boolean handle(Respawn respawn) throws Exception
    {
    	return false;
    }

    public boolean handle(LoginRequest loginRequest) throws Exception
    {
    	return false;
    }

    public boolean handle(ClientSettings settings) throws Exception
    {
    	return false;
    }

    public boolean handle(ClientStatus clientStatus) throws Exception
    {
    	return false;
    }

    public boolean handle(PlayerListItem playerListItem) throws Exception
    {
    	return false;
    }

    public boolean handle(PlayerListHeaderFooter playerListHeaderFooter) throws Exception
    {
    	return false;
    }

    public boolean handle(TabCompleteRequest tabComplete) throws Exception
    {
    	return false;
    }

    public boolean handle(ScoreboardObjective scoreboardObjective) throws Exception
    {
    	return false;
    }

    public boolean handle(ScoreboardScore scoreboardScore) throws Exception
    {
    	return false;
    }

    public boolean handle(EncryptionRequest encryptionRequest) throws Exception
    {
    	return false;
    }

    public boolean handle(ScoreboardDisplay displayScoreboard) throws Exception
    {
    	return false;
    }

    public boolean handle(Team team) throws Exception
    {
    	return false;
    }

    public boolean handle(Title title) throws Exception
    {
    	return false;
    }

    public boolean handle(PluginMessage pluginMessage) throws Exception
    {
    	return false;
    }

    public boolean handle(Kick kick) throws Exception
    {
    	return false;
    }

    public boolean handle(EncryptionResponse encryptionResponse) throws Exception
    {
    	return false;
    }

    public boolean handle(LoginSuccess loginSuccess) throws Exception
    {
    	return false;
    }

    public boolean handle(LegacyHandshake legacyHandshake) throws Exception
    {
    	return false;
    }

    public boolean handle(SetCompression setCompression) throws Exception
    {
    	return false;
    }

    public boolean handle(BossBar bossBar) throws Exception
    {
    	return false;
    }
}
