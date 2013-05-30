package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketD1Team extends DefinedPacket
{

    public String name;
    /**
     * 0 - create, 1 remove, 2 info update, 3 player add, 4 player remove.
     */
    public byte mode;
    public String displayName;
    public String prefix;
    public String suffix;
    public byte friendlyFire;
    public short playerCount;
    public String[] players;

    public PacketD1Team(byte[] buf)
    {
        super( 0xD1, buf );
        name = readUTF();
        mode = readByte();
        if ( mode == 0 || mode == 2 )
        {
            displayName = readUTF();
            prefix = readUTF();
            suffix = readUTF();
            friendlyFire = readByte();
        }
        if ( mode == 0 || mode == 3 || mode == 4 )
        {
            players = new String[ readShort() ];
            for ( int i = 0; i < players.length; i++ )
            {
                players[i] = readUTF();
            }
        }
    }

    public PacketD1Team()
    {
        super( 0xD1 );
    }

    public static PacketD1Team destroy(String name)
    {
        PacketD1Team packet = new PacketD1Team();
        packet.writeString( name );
        packet.writeByte( 1 );
        return packet;
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
