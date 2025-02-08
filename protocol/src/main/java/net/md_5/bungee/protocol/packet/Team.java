package net.md_5.bungee.protocol.packet;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Team extends DefinedPacket
{

    private String name;
    /**
     * 0 - create, 1 remove, 2 info update, 3 player add, 4 player remove.
     */
    private byte mode;
    private Either<String, BaseComponent> displayName;
    private Either<String, BaseComponent> prefix;
    private Either<String, BaseComponent> suffix;
    private NameTagVisibility nameTagVisibility;
    private CollisionRule collisionRule;
    private int color;
    private byte friendlyFire;
    private String[] players;

    /**
     * Packet to destroy a team.
     *
     * @param name team name
     */
    public Team(String name)
    {
        this.name = name;
        this.mode = 1;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        name = readString( buf );
        mode = buf.readByte();
        if ( mode == 0 || mode == 2 )
        {
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
            {
                displayName = readEitherBaseComponent( buf, protocolVersion, true );
                prefix = readEitherBaseComponent( buf, protocolVersion, true );
                suffix = readEitherBaseComponent( buf, protocolVersion, true );
            } else
            {
                displayName = readEitherBaseComponent( buf, protocolVersion, false );
            }
            friendlyFire = buf.readByte();
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_21_5 )
            {
                nameTagVisibility = NameTagVisibility.BY_ID[readVarInt( buf )];
                collisionRule = CollisionRule.BY_ID[readVarInt( buf )];
            } else
            {
                nameTagVisibility = readStringMapKey( buf, NameTagVisibility.BY_NAME );

                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
                {
                    collisionRule = readStringMapKey( buf, CollisionRule.BY_NAME );
                }
            }
            color = ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 ) ? readVarInt( buf ) : buf.readByte();
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                prefix = readEitherBaseComponent( buf, protocolVersion, false );
                suffix = readEitherBaseComponent( buf, protocolVersion, false );
            }
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            int len = readVarInt( buf );
            players = new String[ len ];
            for ( int i = 0; i < len; i++ )
            {
                players[i] = readString( buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( name, buf );
        buf.writeByte( mode );
        if ( mode == 0 || mode == 2 )
        {
            writeEitherBaseComponent( displayName, buf, protocolVersion );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
            {
                writeEitherBaseComponent( prefix, buf, protocolVersion );
                writeEitherBaseComponent( suffix, buf, protocolVersion );
            }
            buf.writeByte( friendlyFire );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_21_5 )
            {
                writeVarInt( nameTagVisibility.ordinal(), buf );
                writeVarInt( collisionRule.ordinal(), buf );
            } else
            {
                writeString( nameTagVisibility.getKey(), buf );
                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
                {
                    writeString( collisionRule.getKey(), buf );
                }
            }

            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                writeVarInt( color, buf );
                writeEitherBaseComponent( prefix, buf, protocolVersion );
                writeEitherBaseComponent( suffix, buf, protocolVersion );
            } else
            {
                buf.writeByte( color );
            }
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            writeVarInt( players.length, buf );
            for ( String player : players )
            {
                writeString( player, buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Getter
    @RequiredArgsConstructor
    public enum NameTagVisibility
    {

        ALWAYS( "always" ),
        NEVER( "never" ),
        HIDE_FOR_OTHER_TEAMS( "hideForOtherTeams" ),
        HIDE_FOR_OWN_TEAM( "hideForOwnTeam" ),
        // 1.9 (and possibly other versions) appear to treat unknown values differently (always render rather than subject to spectator mode, friendly invisibles, etc).
        // we allow the empty value to achieve this in case it is potentially useful even though this is unsupported and its usage may be a bug (#3780).
        UNKNOWN( "" );
        //
        private final String key;
        //
        private static final Map<String, NameTagVisibility> BY_NAME;
        private static final NameTagVisibility[] BY_ID;

        static
        {
            NameTagVisibility[] values = NameTagVisibility.values();
            ImmutableMap.Builder<String, NameTagVisibility> builder = ImmutableMap.builderWithExpectedSize( values.length );

            for ( NameTagVisibility e : values )
            {
                builder.put( e.key, e );
            }

            BY_NAME = builder.build();
            BY_ID = Arrays.copyOf( values, values.length - 1 ); // Ignore dummy UNKNOWN value
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum CollisionRule
    {

        ALWAYS( "always" ),
        NEVER( "never" ),
        PUSH_OTHER_TEAMS( "pushOtherTeams" ),
        PUSH_OWN_TEAM( "pushOwnTeam" );
        //
        private final String key;
        //
        private static final Map<String, CollisionRule> BY_NAME;
        private static final CollisionRule[] BY_ID;

        static
        {
            CollisionRule[] values = BY_ID = CollisionRule.values();
            ImmutableMap.Builder<String, CollisionRule> builder = ImmutableMap.builderWithExpectedSize( values.length );

            for ( CollisionRule e : values )
            {
                builder.put( e.key, e );
            }

            BY_NAME = builder.build();
        }
    }
}
