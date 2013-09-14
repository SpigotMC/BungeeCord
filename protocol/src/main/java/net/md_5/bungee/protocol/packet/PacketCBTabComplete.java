package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCBTabComplete extends DefinedPacket
{

    private String cursor;
    private String[] commands;

    private PacketCBTabComplete()
    {
        super( 0xCB );
    }

    public PacketCBTabComplete(String[] alternatives)
    {
        this();
        commands = alternatives;
    }

    @Override
    public void read(ByteBuf buf)
    {
        cursor = readString( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        String tab = "";
        for ( String alternative : commands )
        {
            if ( tab.isEmpty() )
            {
                tab = alternative + " ";
            } else
            {
                tab += "\0" + alternative + " ";
            }
        }
        writeString( tab, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
