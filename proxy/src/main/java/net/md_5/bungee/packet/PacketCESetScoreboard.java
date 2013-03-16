package net.md_5.bungee.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCESetScoreboard extends DefinedPacket
{

    public String name;
    public String text;
    /**
     * 0 to create, 1 to remove.
     */
    public byte status;

    public PacketCESetScoreboard(String name, String text, byte status)
    {
        super( 0xCE );
        writeUTF( name );
        writeUTF( text );
        writeByte( status );
        this.name = name;
        this.text = text;
        this.status = status;
    }

    PacketCESetScoreboard(byte[] buf)
    {
        super( 0xCE, buf );
        this.name = readUTF();
        this.text = readUTF();
        this.status = readByte();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
