package net.md_5.bungee.protocol;

import com.google.common.base.Preconditions;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Login;
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

public enum Protocol
{

    // Undef
    HANDSHAKE
            {

                {
                    TO_SERVER.registerPacket( 0x00, Handshake.class );
                }
            },
    // 0
    GAME
            {

                {
                    TO_CLIENT.registerPacket( 0x00, KeepAlive.class );
                    TO_CLIENT.registerPacket( 0x01, Login.class );
                    TO_CLIENT.registerPacket( 0x02, Chat.class );
                    TO_CLIENT.registerPacket( 0x07, Respawn.class );
                    TO_CLIENT.registerPacket( 0x38, PlayerListItem.class );
                    TO_CLIENT.registerPacket( 0x3A, TabCompleteResponse.class );
                    TO_CLIENT.registerPacket( 0x3B, ScoreboardObjective.class );
                    TO_CLIENT.registerPacket( 0x3C, ScoreboardScore.class );
                    TO_CLIENT.registerPacket( 0x3D, ScoreboardDisplay.class );
                    TO_CLIENT.registerPacket( 0x3E, Team.class );
                    TO_CLIENT.registerPacket( 0x3F, PluginMessage.class );
                    TO_CLIENT.registerPacket( 0x40, Kick.class );
                    TO_CLIENT.registerPacket( 0x45, Title.class );
                    TO_CLIENT.registerPacket( 0x46, SetCompression.class );
                    TO_CLIENT.registerPacket( 0x47, PlayerListHeaderFooter.class );

                    TO_SERVER.registerPacket( 0x00, KeepAlive.class );
                    TO_SERVER.registerPacket( 0x01, Chat.class );
                    TO_SERVER.registerPacket( 0x14, TabCompleteRequest.class );
                    TO_SERVER.registerPacket( 0x15, ClientSettings.class );
                    TO_SERVER.registerPacket( 0x17, PluginMessage.class );
                }
            },
    // 1
    STATUS
            {

                {
                    TO_CLIENT.registerPacket( 0x00, StatusResponse.class );
                    TO_CLIENT.registerPacket( 0x01, PingPacket.class );

                    TO_SERVER.registerPacket( 0x00, StatusRequest.class );
                    TO_SERVER.registerPacket( 0x01, PingPacket.class );
                }
            },
    //2
    LOGIN
            {

                {
                    TO_CLIENT.registerPacket( 0x00, Kick.class );
                    TO_CLIENT.registerPacket( 0x01, EncryptionRequest.class );
                    TO_CLIENT.registerPacket( 0x02, LoginSuccess.class );
                    TO_CLIENT.registerPacket( 0x03, SetCompression.class );

                    TO_SERVER.registerPacket( 0x00, LoginRequest.class );
                    TO_SERVER.registerPacket( 0x01, EncryptionResponse.class );
                }
            };
    /*========================================================================*/
    public static final int MAX_PACKET_ID = 0xFF;
    public static List<Integer> supportedVersions = Arrays.asList(
            ProtocolConstants.MINECRAFT_1_7_2,
            ProtocolConstants.MINECRAFT_1_7_6,
            ProtocolConstants.MINECRAFT_1_8
    );
    /*========================================================================*/
    public final DirectionData TO_SERVER = new DirectionData( ProtocolConstants.Direction.TO_SERVER );
    public final DirectionData TO_CLIENT = new DirectionData( ProtocolConstants.Direction.TO_CLIENT );

    @RequiredArgsConstructor
    public class DirectionData
    {

        @Getter
        private final ProtocolConstants.Direction direction;
        private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
        private final Class<? extends DefinedPacket>[] packetClasses = new Class[ MAX_PACKET_ID ];
        private final Constructor<? extends DefinedPacket>[] packetConstructors = new Constructor[ MAX_PACKET_ID ];

        public boolean hasPacket(int id)
        {
            return id < MAX_PACKET_ID && packetConstructors[id] != null;
        }

        public final DefinedPacket createPacket(int id)
        {
            if ( id > MAX_PACKET_ID )
            {
                throw new BadPacketException( "Packet with id " + id + " outside of range " );
            }
            if ( packetConstructors[id] == null )
            {
                throw new BadPacketException( "No packet with id " + id );
            }

            try
            {
                return packetConstructors[id].newInstance();
            } catch ( ReflectiveOperationException ex )
            {
                throw new BadPacketException( "Could not construct packet with id " + id, ex );
            }
        }

        protected final void registerPacket(int id, Class<? extends DefinedPacket> packetClass)
        {
            try
            {
                packetConstructors[id] = packetClass.getDeclaredConstructor();
            } catch ( NoSuchMethodException ex )
            {
                throw new BadPacketException( "No NoArgsConstructor for packet class " + packetClass );
            }
            packetClasses[id] = packetClass;
            packetMap.put( packetClass, id );
        }

        protected final void unregisterPacket(int id)
        {
            packetMap.remove( packetClasses[id] );
            packetClasses[id] = null;
            packetConstructors[id] = null;
        }

        final int getId(Class<? extends DefinedPacket> packet)
        {
            Preconditions.checkArgument( packetMap.containsKey( packet ), "Cannot get ID for packet " + packet );

            return packetMap.get( packet );
        }
    }
}
