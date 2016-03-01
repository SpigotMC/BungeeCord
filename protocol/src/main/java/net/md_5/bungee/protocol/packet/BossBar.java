package net.md_5.bungee.protocol.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BossBar extends DefinedPacket
{

    private UUID uuid;
    private Action action;
    private String title;
    private float health;
    private Color color;
    private Division division;
    private byte flags;

    public BossBar(UUID uuid, Action action)
    {
        this.uuid = uuid;
        this.action = action;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        uuid = readUUID( buf );
        action = Action.values()[readVarInt( buf )];

        switch ( action )
        {
            case ADD:
                title = readString( buf );
                health = buf.readFloat();
                color = Color.values()[readVarInt( buf )];
                division = Division.values()[readVarInt( buf )];
                flags = buf.readByte();
                break;
            case HEATLH:
                health = buf.readFloat();
                break;
            case TITLE:
                title = readString( buf );
                break;
            case STYLE:
                color = Color.values()[readVarInt( buf )];
                division = Division.values()[readVarInt( buf )];
                break;
            case FLAGS:
                flags = buf.readByte();
                break;
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeUUID( uuid, buf );
        writeVarInt( action.ordinal(), buf );

        switch ( action )
        {
            case ADD:
                writeString( title, buf );
                buf.writeFloat( health );
                writeVarInt( color.ordinal(), buf );
                writeVarInt( division.ordinal(), buf );
                buf.writeByte( flags );
                break;
            case HEATLH:
                buf.writeFloat( health );
                break;
            case TITLE:
                writeString( title, buf );
                break;
            case STYLE:
                writeVarInt( color.ordinal(), buf );
                writeVarInt( division.ordinal(), buf );
                break;
            case FLAGS:
                buf.writeByte( flags );
                break;
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public static enum Division
    {
        NONE,
        SIX,
        TEN,
        TWELVE,
        TWENTY;
    }

    public static enum Action
    {

        ADD,
        REMOVE,
        HEATLH,
        TITLE,
        STYLE,
        FLAGS;
    }

    public static enum Color
    {

        PINK,
        BLUE,
        RED,
        GREEN,
        YELLOW,
        PURPLE,
        WHITE;
    }
}
