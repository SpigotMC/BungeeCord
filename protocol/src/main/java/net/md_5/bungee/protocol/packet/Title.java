package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.util.ChatComponentDeserializable;
import net.md_5.bungee.protocol.util.ChatDeserializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Title extends DefinedPacket
{

    private Action action;

    // TITLE & SUBTITLE
    private ChatDeserializable textRaw;

    // TIMES
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public Title(Action action)
    {
        this.action = action;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            textRaw = readBaseComponent( buf, protocolVersion );
            return;
        }

        int index = readVarInt( buf );

        // If we're working on 1.10 or lower, increment the value of the index so we pull out the correct value.
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_10 && index >= 2 )
        {
            index++;
        }

        action = Action.values()[index];
        switch ( action )
        {
            case TITLE:
            case SUBTITLE:
            case ACTIONBAR:
                textRaw = readBaseComponent( buf, protocolVersion );
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
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            writeBaseComponent( textRaw, buf, protocolVersion );
            return;
        }

        int index = action.ordinal();

        // If we're working on 1.10 or lower, increment the value of the index so we pull out the correct value.
        if ( protocolVersion <= ProtocolConstants.MINECRAFT_1_10 && index >= 2 )
        {
            index--;
        }

        writeVarInt( index, buf );
        switch ( action )
        {
            case TITLE:
            case SUBTITLE:
            case ACTIONBAR:
                writeBaseComponent( textRaw, buf, protocolVersion );
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
        ACTIONBAR,
        TIMES,
        CLEAR,
        RESET
    }

    public Title(Action action, BaseComponent text, int fadeIn, int stay, int fadeOut)
    {
        setText( text );
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        this.action = action;
    }

    public BaseComponent getText()
    {
        if ( textRaw == null )
        {
            return null;
        }
        return textRaw.get();
    }

    public void setText(BaseComponent text)
    {
        if ( text == null )
        {
            this.textRaw = null;
            return;
        }
        this.textRaw = new ChatComponentDeserializable( text );
    }
}
