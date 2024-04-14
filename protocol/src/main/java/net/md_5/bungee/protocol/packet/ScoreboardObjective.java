package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.data.NumberFormat;
import net.md_5.bungee.protocol.util.ChatComponentDeserializable;
import net.md_5.bungee.protocol.util.ChatDeserializable;
import net.md_5.bungee.protocol.util.Either;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScoreboardObjective extends DefinedPacket
{

    private String name;
    private HealthDisplay type;
    private Either<String, ChatDeserializable> valueRaw;
    /**
     * 0 to create, 1 to remove, 2 to update display text.
     */
    private byte action;
    private NumberFormat numberFormat;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        name = readString( buf );
        action = buf.readByte();
        if ( action == 0 || action == 2 )
        {
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                valueRaw = readEitherBaseComponent( buf, protocolVersion, false );
                type = HealthDisplay.values()[readVarInt( buf )];
            } else
            {
                valueRaw = readEitherBaseComponent( buf, protocolVersion, true );
                type = HealthDisplay.fromString( readString( buf ) );
            }
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
            {
                numberFormat = readNullable( (b) -> readNumberFormat( b, protocolVersion ), buf );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( name, buf );
        buf.writeByte( action );
        if ( action == 0 || action == 2 )
        {
            writeEitherBaseComponent( valueRaw, buf, protocolVersion );
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_13 )
            {
                writeVarInt( type.ordinal(), buf );
            } else
            {
                writeString( type.toString(), buf );
            }
            if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_20_3 )
            {
                writeNullable( numberFormat, (s, b) -> DefinedPacket.writeNumberFormat( s, b, protocolVersion ), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public enum HealthDisplay
    {

        INTEGER, HEARTS;

        @Override
        public String toString()
        {
            return super.toString().toLowerCase( Locale.ROOT );
        }

        public static HealthDisplay fromString(String s)
        {
            return valueOf( s.toUpperCase( Locale.ROOT ) );
        }
    }

    public ScoreboardObjective(String name, Either<String, BaseComponent> value, HealthDisplay type, byte action, NumberFormat numberFormat)
    {
        this.name = name;
        setValue( value );
        this.type = type;
        this.action = action;
        this.numberFormat = numberFormat;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Either<String, BaseComponent> getValue()
    {
        if ( valueRaw == null )
        {
            return null;
        }
        if ( valueRaw.isLeft() )
        {
            return (Either) valueRaw;
        } else
        {
            return Either.right( valueRaw.getRight().get() );
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setValue(Either<String, BaseComponent> value)
    {
        if ( value == null )
        {
            valueRaw = null;
        } else if ( value.isLeft() )
        {
            valueRaw = (Either) value;
        } else
        {
            valueRaw = Either.right( new ChatComponentDeserializable( value.getRight() ) );
        }
    }

}
