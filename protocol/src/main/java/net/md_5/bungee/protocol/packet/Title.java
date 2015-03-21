package net.md_5.bungee.protocol.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Title extends DefinedPacket
{

    private Action action;

    // TITLE & SUBTITLE
    private String text;

    // TIMES
    private int fadeIn;
    private int stay;
    private int fadeOut;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        action = Action.values()[readVarInt( buf )];
        switch ( action )
        {
            case TITLE:
            case SUBTITLE:
                text = readString( buf );
                break;
            case TIMES:
                fadeIn = buf.readInt();
                stay = buf.readInt();
                fadeOut = buf.readInt();
                break;
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( action.ordinal(), buf );
        switch ( action )
        {
            case TITLE:
            case SUBTITLE:
                writeString( text, buf );
                break;
            case TIMES:
                buf.writeInt( fadeIn );
                buf.writeInt( stay );
                buf.writeInt( fadeOut );
                break;
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public static enum Action
    {

        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET
    }
}
