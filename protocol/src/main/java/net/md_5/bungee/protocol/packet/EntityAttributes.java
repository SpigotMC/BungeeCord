package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
public class EntityAttributes extends DefinedPacket
{

    private int entityId;
    private List<Attribute> attributes;

    public EntityAttributes(int entityId)
    {
        this.entityId = entityId;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        entityId = readVarInt( buf );
        int count = buf.readInt();
        attributes = new ArrayList<>( count );
        for ( int i = 0; i < count; i++ )
        {
            Attribute attribute = new Attribute();
            attribute.key = readString( buf );
            attribute.value = buf.readDouble();
            int modifierCount = readVarInt( buf );
            attribute.modifierList = new ArrayList<>( modifierCount );
            for ( int j = 0; j < modifierCount; j++ )
            {
                AttributeModifier modifier = new AttributeModifier();
                modifier.uuid = readUUID( buf );
                modifier.amount = buf.readDouble();
                modifier.operation = buf.readByte();
                attribute.modifierList.add( modifier );
            }
            attributes.add( attribute );
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( entityId, buf );
        buf.writeInt( attributes.size() );
        for ( Attribute attribute : attributes )
        {
            writeString( attribute.key, buf );
            buf.writeDouble( attribute.value );
            writeVarInt( attribute.modifierList.size(), buf );
            for ( AttributeModifier modifier : attribute.modifierList )
            {
                writeUUID( modifier.uuid, buf );
                buf.writeDouble( modifier.amount );
                buf.writeByte( modifier.operation );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attribute
    {

        private String key;
        private double value;
        private List<AttributeModifier> modifierList;
    }

    @Data
    public static class AttributeModifier
    {

        private UUID uuid;
        private double amount;
        private byte operation;
    }

    public static EntityAttributes createDefaultHealth(int entityId, int protocolVersion)
    {
        EntityAttributes packet = new EntityAttributes( entityId );
        String key = "generic.maxHealth"; //TODO Differentiate for 1.16: minecraft:generic.max_health
        packet.attributes = Collections.singletonList( new Attribute( key, 20, Collections.emptyList() ) );
        return packet;
    }
}
