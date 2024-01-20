package net.md_5.bungee.protocol;

import net.md_5.bungee.protocol.packet.LegacyHandshake;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.common.ClientSettings;
import net.md_5.bungee.protocol.packet.common.KeepAlive;
import net.md_5.bungee.protocol.packet.common.Kick;
import net.md_5.bungee.protocol.packet.common.PluginMessage;
import net.md_5.bungee.protocol.packet.common.StoreCookie;
import net.md_5.bungee.protocol.packet.common.Transfer;
import net.md_5.bungee.protocol.packet.configuration.FinishConfiguration;
import net.md_5.bungee.protocol.packet.game.BossBar;
import net.md_5.bungee.protocol.packet.game.Chat;
import net.md_5.bungee.protocol.packet.game.ClearTitles;
import net.md_5.bungee.protocol.packet.game.ClientChat;
import net.md_5.bungee.protocol.packet.game.ClientCommand;
import net.md_5.bungee.protocol.packet.game.ClientStatus;
import net.md_5.bungee.protocol.packet.game.Commands;
import net.md_5.bungee.protocol.packet.game.EntityStatus;
import net.md_5.bungee.protocol.packet.game.GameState;
import net.md_5.bungee.protocol.packet.game.Login;
import net.md_5.bungee.protocol.packet.game.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.game.PlayerListItem;
import net.md_5.bungee.protocol.packet.game.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.game.PlayerListItemUpdate;
import net.md_5.bungee.protocol.packet.game.Respawn;
import net.md_5.bungee.protocol.packet.game.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.game.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.game.ScoreboardScore;
import net.md_5.bungee.protocol.packet.game.ScoreboardScoreReset;
import net.md_5.bungee.protocol.packet.game.ServerData;
import net.md_5.bungee.protocol.packet.game.StartConfiguration;
import net.md_5.bungee.protocol.packet.game.Subtitle;
import net.md_5.bungee.protocol.packet.game.SystemChat;
import net.md_5.bungee.protocol.packet.game.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.game.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.game.Team;
import net.md_5.bungee.protocol.packet.game.Title;
import net.md_5.bungee.protocol.packet.game.TitleTimes;
import net.md_5.bungee.protocol.packet.game.ViewDistance;
import net.md_5.bungee.protocol.packet.handshake.Handshake;
import net.md_5.bungee.protocol.packet.login.EncryptionRequest;
import net.md_5.bungee.protocol.packet.login.EncryptionResponse;
import net.md_5.bungee.protocol.packet.login.LoginAcknowledged;
import net.md_5.bungee.protocol.packet.login.LoginPayloadRequest;
import net.md_5.bungee.protocol.packet.login.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.login.LoginRequest;
import net.md_5.bungee.protocol.packet.login.LoginSuccess;
import net.md_5.bungee.protocol.packet.login.SetCompression;
import net.md_5.bungee.protocol.packet.status.PingPacket;
import net.md_5.bungee.protocol.packet.status.StatusRequest;
import net.md_5.bungee.protocol.packet.status.StatusResponse;

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

    public void handle(ClientChat chat) throws Exception
    {
    }

    public void handle(SystemChat chat) throws Exception
    {
    }

    public void handle(ClientCommand command) throws Exception
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

    public void handle(PlayerListItemRemove playerListItem) throws Exception
    {
    }

    public void handle(PlayerListItemUpdate playerListItem) throws Exception
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

    public void handle(ScoreboardScoreReset scoreboardScoreReset) throws Exception
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

    public void handle(Subtitle title) throws Exception
    {
    }

    public void handle(TitleTimes title) throws Exception
    {
    }

    public void handle(ClearTitles title) throws Exception
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

    public void handle(BossBar bossBar) throws Exception
    {
    }

    public void handle(LoginPayloadRequest request) throws Exception
    {
    }

    public void handle(LoginPayloadResponse response) throws Exception
    {
    }

    public void handle(EntityStatus status) throws Exception
    {
    }

    public void handle(Commands commands) throws Exception
    {
    }

    public void handle(ViewDistance viewDistance) throws Exception
    {
    }

    public void handle(GameState gameState) throws Exception
    {
    }

    public void handle(ServerData serverData) throws Exception
    {
    }

    public void handle(LoginAcknowledged loginAcknowledged) throws Exception
    {
    }

    public void handle(StartConfiguration startConfiguration) throws Exception
    {
    }

    public void handle(FinishConfiguration finishConfiguration) throws Exception
    {
    }

    public void handle(Transfer transfer) throws Exception
    {
    }

    public void handle(StoreCookie storeCookie) throws Exception
    {
    }
}
