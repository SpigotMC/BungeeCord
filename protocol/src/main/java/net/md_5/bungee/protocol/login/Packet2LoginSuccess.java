package net.md_5.bungee.protocol.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Packet2LoginSuccess extends DefinedPacket
{

    private String data;

    @Override
    public void read(ByteBuf buf)
    {
        data = readString( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( data, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
