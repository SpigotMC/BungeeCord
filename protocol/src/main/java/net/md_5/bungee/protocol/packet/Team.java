package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.Either;

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
    //
    private Either<String, NameTagVisibility> nameTagVisibility;
    private Either<String, CollisionRule> collisionRule;
    //
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
                nameTagVisibility = Either.right( NameTagVisibility.BY_ID[readVarInt( buf )] );
                collisionRule = Either.right( CollisionRule.BY_ID[readVarInt( buf )] );
            } else
            {
                nameTagVisibility = Either.left( readString( buf ) );
                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
                {
                    collisionRule = Either.left( readString( buf ) );
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
                writeVarInt( nameTagVisibility.getRight().ordinal(), buf );
                writeVarInt( collisionRule.getRight().ordinal(), buf );
            } else
            {
                writeString( nameTagVisibility.getLeft(), buf );
                if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
                {
                    writeString( collisionRule.getLeft(), buf );
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
        HIDE_FOR_OWN_TEAM( "hideForOwnTeam" );
        //
        private final String key;
        //
        private static final NameTagVisibility[] BY_ID = values();
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
        private static final CollisionRule[] BY_ID = CollisionRule.values();
    }
}
