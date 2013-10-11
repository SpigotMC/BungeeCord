package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

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
    private String displayName;
    private String prefix;
    private String suffix;
    private boolean friendlyFire;
    private short playerCount;
    private String[] players;

    /**
     * Packet to destroy a team.
     *
     * @param name
     */
    public Team(String name)
    {
        this();
        this.name = name;
        this.mode = 1;
    }

    @Override
    public void read(ByteBuf buf)
    {
        name = readString( buf );
        mode = buf.readByte();
        if ( mode == 0 || mode == 2 )
        {
            displayName = readString( buf );
            prefix = readString( buf );
            suffix = readString( buf );
            friendlyFire = buf.readBoolean();
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            players = new String[ buf.readShort() ];
            for ( int i = 0; i < getPlayers().length; i++ )
            {
                players[i] = readString( buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( name, buf );
        buf.writeByte( mode );
        if ( mode == 0 || mode == 2 )
        {
            writeString( displayName, buf );
            writeString( prefix, buf );
            writeString( suffix, buf );
            buf.writeBoolean( friendlyFire );
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            buf.writeShort( players.length );
            for ( int i = 0; i < players.length; i++ )
            {
                writeString( players[i], buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
