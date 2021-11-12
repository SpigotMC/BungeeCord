package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientSettings extends DefinedPacket
{

    private String locale;
    private byte viewDistance;
    private int chatFlags;
    private boolean chatColours;
    private byte difficulty;
    private byte skinParts;
    private int mainHand;
    private boolean disableTextFiltering;
    private boolean allowServerListing;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        locale = readString( buf, 16 );
        viewDistance = buf.readByte();
        chatFlags = protocolVersion >= ProtocolConstants.MINECRAFT_1_9 ? DefinedPacket.readVarInt( buf ) : buf.readUnsignedByte();
        chatColours = buf.readBoolean();
        skinParts = buf.readByte();
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            mainHand = DefinedPacket.readVarInt( buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            disableTextFiltering = buf.readBoolean();
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18 )
        {
            allowServerListing = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeString( locale, buf );
        buf.writeByte( viewDistance );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            DefinedPacket.writeVarInt( chatFlags, buf );
        } else
        {
            buf.writeByte( chatFlags );
        }
        buf.writeBoolean( chatColours );
        buf.writeByte( skinParts );
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_9 )
        {
            DefinedPacket.writeVarInt( mainHand, buf );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_17 )
        {
            buf.writeBoolean( disableTextFiltering );
        }
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_18 )
        {
            buf.writeBoolean( allowServerListing );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
