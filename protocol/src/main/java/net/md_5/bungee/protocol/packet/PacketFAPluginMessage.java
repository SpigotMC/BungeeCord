package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketFAPluginMessage extends DefinedPacket
{

    public String tag;
    public byte[] data;

    public PacketFAPluginMessage(String tag, byte[] data)
    {
        super( 0xFA );
        writeString( tag );
        writeArray( data );
        this.tag = tag;
        this.data = data;
    }

    PacketFAPluginMessage(byte[] buf)
    {
        super( 0xFA, buf );
        this.tag = readUTF();
        this.data = readArray();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
