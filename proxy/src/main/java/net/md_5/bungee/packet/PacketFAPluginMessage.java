package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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

    public PacketFAPluginMessage(ByteBuf buf)
    {
        super( 0xFA, buf );
        this.tag = readString();
        this.data = readArray();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
