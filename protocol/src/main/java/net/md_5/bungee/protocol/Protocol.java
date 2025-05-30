package net.md_5.bungee.protocol;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.function.Supplier;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClearDialog;
import net.md_5.bungee.protocol.packet.ClearTitles;
import net.md_5.bungee.protocol.packet.ClientChat;
import net.md_5.bungee.protocol.packet.ClientCommand;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.Commands;
import net.md_5.bungee.protocol.packet.CookieRequest;
import net.md_5.bungee.protocol.packet.CookieResponse;
import net.md_5.bungee.protocol.packet.CustomClickAction;
import net.md_5.bungee.protocol.packet.DisconnectReportDetails;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.FinishConfiguration;
import net.md_5.bungee.protocol.packet.GameState;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginAcknowledged;
import net.md_5.bungee.protocol.packet.LoginPayloadRequest;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.ScoreboardScoreReset;
import net.md_5.bungee.protocol.packet.ServerData;
import net.md_5.bungee.protocol.packet.ServerLinks;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.ShowDialog;
import net.md_5.bungee.protocol.packet.StartConfiguration;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.protocol.packet.StoreCookie;
import net.md_5.bungee.protocol.packet.Subtitle;
import net.md_5.bungee.protocol.packet.SystemChat;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.Team;
import net.md_5.bungee.protocol.packet.Title;
import net.md_5.bungee.protocol.packet.TitleTimes;
import net.md_5.bungee.protocol.packet.Transfer;
import net.md_5.bungee.protocol.packet.UnsignedClientCommand;
import net.md_5.bungee.protocol.packet.ViewDistance;

public enum Protocol
{

    // Undef
    HANDSHAKE
    {

        {
            TO_SERVER.registerPacket(
                    Handshake.class,
                    Handshake::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
        }
    },
    // 0
    GAME
    {

        {
            TO_CLIENT.registerPacket(
                    KeepAlive.class,
                    KeepAlive::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x21 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x20 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x21 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x20 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x21 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x1E ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x20 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x23 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x24 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x26 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x27 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x26 )
            );
            TO_CLIENT.registerPacket(
                    Login.class,
                    Login::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x23 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x25 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x26 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x25 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x24 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x26 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x23 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x25 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x24 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x28 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x29 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x2B ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x2C ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x2B )
            );
            TO_CLIENT.registerPacket( Chat.class,
                    Chat::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_19, -1 )
            );
            TO_CLIENT.registerPacket(
                    Respawn.class,
                    Respawn::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x07 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x33 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x34 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x35 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x38 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x39 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x3D ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x3E ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x3D ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x43 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x45 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x4C ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x4B )
            );
            TO_CLIENT.registerPacket(
                    BossBar.class,
                    BossBar::new,
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x09 )
            );
            TO_CLIENT.registerPacket(
                    PlayerListItem.class, // PlayerInfo
                    PlayerListItem::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x38 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x2D ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x2E ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x30 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x33 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x34 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x33 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x32 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x36 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x34 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x37 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, -1 )
            );
            TO_CLIENT.registerPacket(
                    TabCompleteResponse.class,
                    TabCompleteResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x0F )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardObjective.class,
                    ScoreboardObjective::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x3F ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x45 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x49 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x4A ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x53 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x56 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x54 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x58 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x5A ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x5C ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x5E ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x64 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x63 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardScore.class,
                    ScoreboardScore::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3C ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x44 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x45 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x48 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x4C ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x4D ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x56 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x59 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x57 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x5B ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x5D ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x5F ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x61 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x68 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x67 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardScoreReset.class,
                    ScoreboardScoreReset::new,
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x44 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x49 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x48 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardDisplay.class,
                    ScoreboardDisplay::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3D ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x38 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x3E ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x43 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x4C ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x4F ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x4D ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x51 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x53 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x55 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x57 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x5C ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x5B )
            );
            TO_CLIENT.registerPacket(
                    Team.class,
                    Team::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3E ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x43 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x44 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x4B ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x4C ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x55 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x58 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x56 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x5A ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x5C ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x5E ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x60 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x67 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x66 )
            );
            TO_CLIENT.registerPacket(
                    PluginMessage.class,
                    PluginMessage::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x3F ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x15 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x16 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x15 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x18 )
            );
            TO_CLIENT.registerPacket(
                    Kick.class,
                    Kick::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x40 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x1D ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x1C )
            );
            TO_CLIENT.registerPacket(
                    Title.class,
                    Title::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x45 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x48 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x4B ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x4F ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x50 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x4F ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x59 ),
                    map( ProtocolConstants.MINECRAFT_1_18, 0x5A ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x5D ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x5B ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x5F ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x61 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x63 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x65 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x6C ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x6B )
            );
            TO_CLIENT.registerPacket(
                    ClearTitles.class,
                    ClearTitles::new,
                    map( ProtocolConstants.MINECRAFT_1_17, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x0E )
            );
            TO_CLIENT.registerPacket(
                    Subtitle.class,
                    Subtitle::new,
                    map( ProtocolConstants.MINECRAFT_1_17, 0x57 ),
                    map( ProtocolConstants.MINECRAFT_1_18, 0x58 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x5B ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x59 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x5D ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x5F ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x61 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x63 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x6A ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x69 )
            );
            TO_CLIENT.registerPacket(
                    TitleTimes.class,
                    TitleTimes::new,
                    map( ProtocolConstants.MINECRAFT_1_17, 0x5A ),
                    map( ProtocolConstants.MINECRAFT_1_18, 0x5B ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x5E ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x5C ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x60 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x62 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x64 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x66 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x6D ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x6C )
            );
            TO_CLIENT.registerPacket(
                    SystemChat.class,
                    SystemChat::new,
                    map( ProtocolConstants.MINECRAFT_1_19, 0x5F ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x62 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x60 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x64 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x67 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x69 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x6C ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x73 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x72 )
            );
            TO_CLIENT.registerPacket(
                    PlayerListHeaderFooter.class,
                    PlayerListHeaderFooter::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x48 ),
                    map( ProtocolConstants.MINECRAFT_1_9_4, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x49 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x4A ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x4E ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x53 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x54 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x53 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x5E ),
                    map( ProtocolConstants.MINECRAFT_1_18, 0x5F ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x60 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x63 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x61 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x65 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x68 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x6A ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x6D ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x74 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x73 )
            );
            TO_CLIENT.registerPacket(
                    EntityStatus.class,
                    EntityStatus::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x1C ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x1C ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x19 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x1C ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x1D ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x1E )
            );
            TO_CLIENT.registerPacket(
                    Commands.class,
                    Commands::new,
                    map( ProtocolConstants.MINECRAFT_1_13, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x12 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x12 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x10 )
            );
            TO_CLIENT.registerPacket(
                    GameState.class,
                    GameState::new,
                    map( ProtocolConstants.MINECRAFT_1_15, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x1E ),
                    map( ProtocolConstants.MINECRAFT_1_16_2, 0x1D ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x1E ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x1B ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x1D ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x1C ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x1F ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x20 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x22 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x23 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x22 )
            );
            TO_CLIENT.registerPacket(
                    ViewDistance.class,
                    ViewDistance::new,
                    map( ProtocolConstants.MINECRAFT_1_14, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_15, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x4A ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x49 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x4C ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x4B ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x4F ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x51 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x53 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x55 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x59 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x58 )
            );
            TO_CLIENT.registerPacket(
                    ServerData.class,
                    ServerData::new,
                    map( ProtocolConstants.MINECRAFT_1_19, 0x3F ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x42 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x41 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x45 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x47 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x49 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x4B ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x50 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x4F )
            );
            TO_CLIENT.registerPacket(
                    PlayerListItemRemove.class,
                    PlayerListItemRemove::new,
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x35 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x39 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x3B ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x3D ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x3F ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x3E )
            );
            TO_CLIENT.registerPacket(
                    PlayerListItemUpdate.class,
                    PlayerListItemUpdate::new,
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x36 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x3A ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x3C ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x3E ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x40 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x3F )
            );
            TO_CLIENT.registerPacket(
                    StartConfiguration.class,
                    StartConfiguration::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x65 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x67 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x69 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x70 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x6F )
            );
            TO_CLIENT.registerPacket(
                    CookieRequest.class,
                    CookieRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x16 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x15 )
            );
            TO_CLIENT.registerPacket(
                    StoreCookie.class,
                    StoreCookie::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x6B ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x72 ),
                    map( ProtocolConstants.MINECRAFT_1_21_5, 0x71 )
            );
            TO_CLIENT.registerPacket(
                    Transfer.class,
                    Transfer::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x73 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x7A )
            );
            TO_CLIENT.registerPacket(
                    DisconnectReportDetails.class,
                    DisconnectReportDetails::new,
                    map( ProtocolConstants.MINECRAFT_1_21, 0x7A ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x81 )
            );
            TO_CLIENT.registerPacket(
                    ServerLinks.class,
                    ServerLinks::new,
                    map( ProtocolConstants.MINECRAFT_1_21, 0x7B ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x82 )
            );
            TO_CLIENT.registerPacket(
                    ClearDialog.class,
                    ClearDialog::new,
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x84 )
            );
            TO_CLIENT.registerPacket(
                    ShowDialog.class,
                    ShowDialog::new,
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x85 )
            );

            TO_SERVER.registerPacket(
                    KeepAlive.class,
                    KeepAlive::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_16, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x12 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x12 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x14 ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x15 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x18 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x1A ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x1B )
            );
            TO_SERVER.registerPacket( Chat.class,
                    Chat::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x03 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x03 ),
                    map( ProtocolConstants.MINECRAFT_1_19, -1 )
            );
            TO_SERVER.registerPacket(
                    ClientCommand.class,
                    ClientCommand::new,
                    map( ProtocolConstants.MINECRAFT_1_19, 0x03 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x04 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x06 ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x07 )
            );
            TO_SERVER.registerPacket(
                    UnsignedClientCommand.class,
                    UnsignedClientCommand::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x04 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x06 )
            );
            TO_SERVER.registerPacket(
                    ClientChat.class,
                    ClientChat::new,
                    map( ProtocolConstants.MINECRAFT_1_19, 0x04 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x06 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x07 ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x08 )
            );
            TO_SERVER.registerPacket(
                    TabCompleteRequest.class,
                    TabCompleteRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x14 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x06 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x08 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x09 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x08 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x09 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x0E )
            );
            TO_SERVER.registerPacket(
                    ClientSettings.class,
                    ClientSettings::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x15 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x04 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x04 ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x05 ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x07 ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x08 ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x07 ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x08 ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x09 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x0D )
            );
            TO_SERVER.registerPacket(
                    PluginMessage.class,
                    PluginMessage::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x17 ),
                    map( ProtocolConstants.MINECRAFT_1_9, 0x09 ),
                    map( ProtocolConstants.MINECRAFT_1_12, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_12_1, 0x09 ),
                    map( ProtocolConstants.MINECRAFT_1_13, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_14, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_17, 0x0A ),
                    map( ProtocolConstants.MINECRAFT_1_19, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_19_1, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_19_3, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_19_4, 0x0D ),
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x0F ),
                    map( ProtocolConstants.MINECRAFT_1_20_3, 0x10 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x12 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x14 ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x15 )
            );
            TO_SERVER.registerPacket(
                    StartConfiguration.class,
                    StartConfiguration::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x0B ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x0C ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x0E ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x0F )
            );
            TO_SERVER.registerPacket(
                    CookieResponse.class,
                    CookieResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x11 ),
                    map( ProtocolConstants.MINECRAFT_1_21_2, 0x13 ),
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x14 )
            );
            TO_SERVER.registerPacket(
                    CustomClickAction.class,
                    CustomClickAction::new,
                    map( ProtocolConstants.MINECRAFT_1_21_6, 0x41 )
            );
        }
    },
    // 1
    STATUS
    {

        {
            TO_CLIENT.registerPacket(
                    StatusResponse.class,
                    StatusResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    PingPacket.class,
                    PingPacket::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );

            TO_SERVER.registerPacket(
                    StatusRequest.class,
                    StatusRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    PingPacket.class,
                    PingPacket::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
        }
    },
    //2
    LOGIN
    {

        {
            TO_CLIENT.registerPacket(
                    Kick.class,
                    Kick::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    EncryptionRequest.class,
                    EncryptionRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
            TO_CLIENT.registerPacket(
                    LoginSuccess.class,
                    LoginSuccess::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x02 )
            );
            TO_CLIENT.registerPacket(
                    SetCompression.class,
                    SetCompression::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x03 )
            );
            TO_CLIENT.registerPacket(
                    LoginPayloadRequest.class,
                    LoginPayloadRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_13, 0x04 )
            );
            TO_CLIENT.registerPacket(
                    CookieRequest.class,
                    CookieRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x05 )
            );

            TO_SERVER.registerPacket(
                    LoginRequest.class,
                    LoginRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    EncryptionResponse.class,
                    EncryptionResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_8, 0x01 )
            );
            TO_SERVER.registerPacket(
                    LoginPayloadResponse.class,
                    LoginPayloadResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_13, 0x02 )
            );
            TO_SERVER.registerPacket(
                    LoginAcknowledged.class,
                    LoginAcknowledged::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x03 )
            );
            TO_SERVER.registerPacket(
                    CookieResponse.class,
                    CookieResponse::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x04 )
            );
        }
    },
    // 3
    CONFIGURATION
    {

        {

            TO_CLIENT.registerPacket(
                    CookieRequest.class,
                    CookieRequest::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    PluginMessage.class,
                    PluginMessage::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x00 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x01 )
            );
            TO_CLIENT.registerPacket(
                    Kick.class,
                    Kick::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x02 )
            );
            TO_CLIENT.registerPacket(
                    FinishConfiguration.class,
                    FinishConfiguration::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x03 )
            );
            TO_CLIENT.registerPacket(
                    KeepAlive.class,
                    KeepAlive::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x03 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x04 )
            );
            TO_CLIENT.registerPacket(
                    StoreCookie.class,
                    StoreCookie::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x0A )
            );
            TO_CLIENT.registerPacket(
                    Transfer.class,
                    Transfer::new,
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x0B )
            );
            TO_CLIENT.registerPacket(
                    DisconnectReportDetails.class,
                    DisconnectReportDetails::new,
                    map( ProtocolConstants.MINECRAFT_1_21, 0x0F )
            );
            TO_CLIENT.registerPacket(
                    ServerLinks.class,
                    ServerLinks::new,
                    map( ProtocolConstants.MINECRAFT_1_21, 0x10 )
            );

            TO_SERVER.registerPacket(
                    ClientSettings.class,
                    ClientSettings::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x00 )
            );
            TO_SERVER.registerPacket(
                    PluginMessage.class,
                    PluginMessage::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x01 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x02 )
            );
            TO_SERVER.registerPacket(
                    FinishConfiguration.class,
                    FinishConfiguration::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x02 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x03 )
            );
            TO_SERVER.registerPacket(
                    KeepAlive.class,
                    KeepAlive::new,
                    map( ProtocolConstants.MINECRAFT_1_20_2, 0x03 ),
                    map( ProtocolConstants.MINECRAFT_1_20_5, 0x04 )
            );
        }
    };
    /*========================================================================*/
    public static final int MAX_PACKET_ID = 0xFF;
    /*========================================================================*/
    public final DirectionData TO_SERVER = new DirectionData( this, ProtocolConstants.Direction.TO_SERVER );
    public final DirectionData TO_CLIENT = new DirectionData( this, ProtocolConstants.Direction.TO_CLIENT );

    public static void main(String[] args)
    {
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            dump( version );
        }
    }

    private static void dump(int version)
    {
        for ( Protocol protocol : Protocol.values() )
        {
            dump( version, protocol );
        }
    }

    private static void dump(int version, Protocol protocol)
    {
        dump( version, protocol.TO_CLIENT );
        dump( version, protocol.TO_SERVER );
    }

    private static void dump(int version, DirectionData data)
    {
        for ( int id = 0; id < MAX_PACKET_ID; id++ )
        {
            DefinedPacket packet = data.createPacket( id, version );
            if ( packet != null )
            {
                System.out.println( version + " " + data.protocolPhase + " " + data.direction + " " + id + " " + packet.getClass().getSimpleName() );
            }
        }
    }

    @Data
    private static class ProtocolData
    {

        private final int protocolVersion;
        private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
        @SuppressWarnings("unchecked")
        private final Supplier<? extends DefinedPacket>[] packetConstructors = new Supplier[ MAX_PACKET_ID ];
    }

    @Data
    private static class ProtocolMapping
    {

        private final int protocolVersion;
        private final int packetID;
    }

    // Helper method
    private static ProtocolMapping map(int protocol, int id)
    {
        return new ProtocolMapping( protocol, id );
    }

    public static final class DirectionData
    {

        private final TIntObjectMap<ProtocolData> protocols = new TIntObjectHashMap<>();
        //
        private final Protocol protocolPhase;
        @Getter
        private final ProtocolConstants.Direction direction;

        public DirectionData(Protocol protocolPhase, ProtocolConstants.Direction direction)
        {
            this.protocolPhase = protocolPhase;
            this.direction = direction;

            for ( int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS )
            {
                protocols.put( protocol, new ProtocolData( protocol ) );
            }
        }

        private ProtocolData getProtocolData(int version)
        {
            ProtocolData protocol = protocols.get( version );
            if ( protocol == null && ( protocolPhase != Protocol.GAME ) )
            {
                protocol = Iterables.getFirst( protocols.valueCollection(), null );
            }
            return protocol;
        }

        public final DefinedPacket createPacket(int id, int version)
        {
            ProtocolData protocolData = getProtocolData( version );
            if ( protocolData == null )
            {
                throw new BadPacketException( "Unsupported protocol version " + version );
            }
            if ( id > MAX_PACKET_ID || id < 0 )
            {
                throw new BadPacketException( "Packet with id " + id + " outside of range" );
            }

            Supplier<? extends DefinedPacket> constructor = protocolData.packetConstructors[id];
            return ( constructor == null ) ? null : constructor.get();
        }

        private void registerPacket(Class<? extends DefinedPacket> packetClass, Supplier<? extends DefinedPacket> constructor, ProtocolMapping... mappings)
        {
            int mappingIndex = 0;
            ProtocolMapping mapping = mappings[mappingIndex];
            for ( int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS )
            {
                if ( protocol < mapping.protocolVersion )
                {
                    // This is a new packet, skip it till we reach the next protocol
                    continue;
                }

                if ( mapping.protocolVersion < protocol && mappingIndex + 1 < mappings.length )
                {
                    // Mapping is non current, but the next one may be ok
                    ProtocolMapping nextMapping = mappings[mappingIndex + 1];

                    if ( nextMapping.protocolVersion == protocol )
                    {
                        Preconditions.checkState( nextMapping.packetID != mapping.packetID, "Duplicate packet mapping (%s, %s)", mapping.protocolVersion, nextMapping.protocolVersion );

                        mapping = nextMapping;
                        mappingIndex++;
                    }
                }

                if ( mapping.packetID < 0 )
                {
                    break;
                }

                ProtocolData data = protocols.get( protocol );
                Preconditions.checkState( data.packetConstructors[mapping.packetID] == null, "Duplicate packet mapping (%s)", mapping.packetID );

                data.packetMap.put( packetClass, mapping.packetID );
                data.packetConstructors[mapping.packetID] = constructor;
            }
        }

        public boolean hasPacket(Class<? extends DefinedPacket> packet, int version)
        {
            ProtocolData protocolData = getProtocolData( version );
            if ( protocolData == null )
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }

            return protocolData.packetMap.containsKey( packet );
        }

        final int getId(Class<? extends DefinedPacket> packet, int version)
        {

            ProtocolData protocolData = getProtocolData( version );
            if ( protocolData == null )
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }
            Preconditions.checkArgument( protocolData.packetMap.containsKey( packet ), "Cannot get ID for packet %s in phase %s with direction %s", packet, protocolPhase, direction );

            return protocolData.packetMap.get( packet );
        }
    }
}
