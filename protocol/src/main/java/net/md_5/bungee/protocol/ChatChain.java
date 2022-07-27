package net.md_5.bungee.protocol;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChatChain extends DefinedPacket
{

    private List<ChainLink> seen;
    private List<ChainLink> received;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        seen = readLinks( buf );
        if ( buf.readBoolean() )
        {
            received = readLinks( buf );
        }
    }

    private static List<ChainLink> readLinks(ByteBuf buf)
    {
        int cnt = readVarInt( buf );
        Preconditions.checkArgument( cnt <= 5, "Too many entries" );
        List<ChainLink> chain = new LinkedList<>();
        for ( int i = 0; i < cnt; i++ )
        {
            chain.add( new ChainLink( readUUID( buf ), readArray( buf ) ) );
        }
        return chain;
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeLinks( seen, buf );
        if ( received != null )
        {
            buf.writeBoolean( true );
            writeLinks( received, buf );
        } else
        {
            buf.writeBoolean( false );
        }
    }

    private static void writeLinks(List<ChainLink> links, ByteBuf buf)
    {
        writeVarInt( links.size(), buf );
        for ( ChainLink link : links )
        {
            writeUUID( link.sender, buf );
            writeArray( link.signature, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        throw new UnsupportedOperationException( "Not supported." );
    }

    @Data
    public static class ChainLink
    {

        private final UUID sender;
        private final byte[] signature;
    }
}
