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
public class TabComplete extends DefinedPacket
{

    private String cursor;
    private String[] commands;

    public TabComplete(String[] alternatives)
    {
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
        StringBuilder tab = new StringBuilder();
        for ( String alternative : commands )
        {
            tab.append( alternative );
            tab.append( "\00" );
        }
        writeString( tab.substring( 0, tab.length() - 1 ), buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
