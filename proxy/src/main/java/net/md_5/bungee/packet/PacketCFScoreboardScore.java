package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
