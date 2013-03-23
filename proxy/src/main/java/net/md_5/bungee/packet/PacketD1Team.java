package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
