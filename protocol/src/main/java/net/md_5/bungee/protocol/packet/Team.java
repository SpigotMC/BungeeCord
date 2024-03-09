package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.Deserializable;
import net.md_5.bungee.protocol.util.Either;
import net.md_5.bungee.protocol.util.NoOrigDeserializable;

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
    private Either<String, Deserializable<Either<String, TypedTag>, BaseComponent>> displayNameRaw;
    private Either<String, Deserializable<Either<String, TypedTag>, BaseComponent>> prefixRaw;
    private Either<String, Deserializable<Either<String, TypedTag>, BaseComponent>> suffixRaw;
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
                displayNameRaw = readEitherBaseComponent( buf, protocolVersion, true );
                prefixRaw = readEitherBaseComponent( buf, protocolVersion, true );
                suffixRaw = readEitherBaseComponent( buf, protocolVersion, true );
            } else
            {
                displayNameRaw = readEitherBaseComponent( buf, protocolVersion, false );
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
                prefixRaw = readEitherBaseComponent( buf, protocolVersion, false );
                suffixRaw = readEitherBaseComponent( buf, protocolVersion, false );
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
            writeEitherBaseComponent( displayNameRaw, buf, protocolVersion );
            if ( protocolVersion < ProtocolConstants.MINECRAFT_1_13 )
            {
                writeEitherBaseComponent( prefixRaw, buf, protocolVersion );
                writeEitherBaseComponent( suffixRaw, buf, protocolVersion );
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
                writeEitherBaseComponent( prefixRaw, buf, protocolVersion );
                writeEitherBaseComponent( suffixRaw, buf, protocolVersion );
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

    @SuppressWarnings("unchecked")
    public Either<String, BaseComponent> getDisplayName()
    {
        if ( displayNameRaw == null )
        {
            return null;
        }
        if ( displayNameRaw.isLeft() )
        {
            return (Either) displayNameRaw;
        } else
        {
            return Either.right( displayNameRaw.getRight().get() );
        }
    }

    @SuppressWarnings("unchecked")
    public void setDisplayName(Either<String, BaseComponent> displayName)
    {
        if ( displayName == null )
        {
            displayNameRaw = null;
            return;
        }
        if ( displayName.isLeft() )
        {
            displayNameRaw = (Either) displayName;
        } else
        {
            displayNameRaw = Either.right( new NoOrigDeserializable<>( displayName.getRight() ) );
        }
    }

    @SuppressWarnings("unchecked")
    public Either<String, BaseComponent> getPrefix()
    {
        if ( prefixRaw == null )
        {
            return null;
        }
        if ( prefixRaw.isLeft() )
        {
            return (Either) prefixRaw;
        } else
        {
            return Either.right( prefixRaw.getRight().get() );
        }
    }

    @SuppressWarnings("unchecked")
    public void setPrefix(Either<String, BaseComponent> prefix)
    {
        if ( prefix == null )
        {
            prefixRaw = null;
            return;
        }
        if ( prefix.isLeft() )
        {
            prefixRaw = (Either) prefix;
        } else
        {
            prefixRaw = Either.right( new NoOrigDeserializable<>( prefix.getRight() ) );
        }
    }

    @SuppressWarnings("unchecked")
    public Either<String, BaseComponent> getSuffix()
    {
        if ( suffixRaw == null )
        {
            return null;
        }
        if ( suffixRaw.isLeft() )
        {
            return (Either) suffixRaw;
        } else
        {
            return Either.right( suffixRaw.getRight().get() );
        }
    }

    @SuppressWarnings("unchecked")
    public void setSuffix(Either<String, BaseComponent> suffix)
    {
        if ( suffix == null )
        {
            suffixRaw = null;
            return;
        }
        if ( suffix.isLeft() )
        {
            suffixRaw = (Either) suffix;
        } else
        {
            suffixRaw = Either.right( new NoOrigDeserializable<>( suffix.getRight() ) );
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
