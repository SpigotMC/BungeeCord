package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
    private String nameTagVisibility;
    private String collisionRule;
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
            nameTagVisibility = readString( buf );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                collisionRule = readString( buf );
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
            writeString( nameTagVisibility, buf );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
            {
                writeString( collisionRule, buf );
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
}
