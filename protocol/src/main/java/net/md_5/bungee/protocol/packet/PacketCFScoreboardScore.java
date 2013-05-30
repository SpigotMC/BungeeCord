package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCFScoreboardScore extends DefinedPacket
{

    public String itemName;
    /**
     * 0 = create / update, 1 = remove.
     */
    public byte action;
    public String scoreName;
    public int value;

    public PacketCFScoreboardScore(byte[] buf)
    {
        super( 0xCF, buf );
        itemName = readUTF();
        action = readByte();
        if ( action == 0 )
        {
            scoreName = readUTF();
            value = readInt();
        }
    }

    public PacketCFScoreboardScore(String itemName, byte action, String scoreName, int value)
    {
        super( 0xCF );
        writeString( itemName );
        writeByte( action );
        if ( action == 0 )
        {
            writeString( scoreName );
            writeInt( value );
        }
        this.itemName = itemName;
        this.action = action;
        this.scoreName = scoreName;
        this.value = value;
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
