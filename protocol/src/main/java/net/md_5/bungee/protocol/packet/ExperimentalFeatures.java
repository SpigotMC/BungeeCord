package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExperimentalFeatures extends DefinedPacket
{
    private List<String> features = new ArrayList<>();

    @Override
    public void read(ByteBuf buf)
    {
        for ( int i = 0; i < readVarInt( buf ); i++ )
        {
            features.add( readString( buf ) );
        }
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( features.size(), buf );
        for ( String feature : features )
        {
            writeString( feature, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
