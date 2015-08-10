package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerListItem extends DefinedPacket
{

    private Action action;
    private Item[] items;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        action = Action.values()[ DefinedPacket.readVarInt( buf )];
        items = new Item[ DefinedPacket.readVarInt( buf ) ];
        for ( int i = 0; i < items.length; i++ )
        {
            Item item = items[ i ] = new Item();
            item.setUuid( DefinedPacket.readUUID( buf ) );
            switch ( action )
            {
                case ADD_PLAYER:
                    item.username = DefinedPacket.readString( buf );
                    item.properties = new String[ DefinedPacket.readVarInt( buf ) ][];
                    for ( int j = 0; j < item.properties.length; j++ )
                    {
                        String name = DefinedPacket.readString( buf );
                        String value = DefinedPacket.readString( buf );
                        if ( buf.readBoolean() )
                        {
                            item.properties[ j] = new String[]
                            {
                                name, value, DefinedPacket.readString( buf )
                            };
                        } else
                        {
                            item.properties[ j ] = new String[]
                            {
                                name, value
                            };
                        }
                    }
                    item.gamemode = DefinedPacket.readVarInt( buf );
                    item.ping = DefinedPacket.readVarInt( buf );
                    if ( buf.readBoolean() )
                    {
                        item.displayName = DefinedPacket.readString( buf );
                    }
                    break;
                case UPDATE_GAMEMODE:
                    item.gamemode = DefinedPacket.readVarInt( buf );
                    break;
                case UPDATE_LATENCY:
                    item.ping = DefinedPacket.readVarInt( buf );
                    break;
                case UPDATE_DISPLAY_NAME:
                    if ( buf.readBoolean() )
                    {
                        item.displayName = DefinedPacket.readString( buf );
                    }
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        DefinedPacket.writeVarInt( action.ordinal(), buf );
        DefinedPacket.writeVarInt( items.length, buf );
        for ( Item item : items )
        {
            DefinedPacket.writeUUID( item.uuid, buf );
            switch ( action )
            {
                case ADD_PLAYER:
                    DefinedPacket.writeString( item.username, buf );
                    DefinedPacket.writeVarInt( item.properties.length, buf );
                    for ( String[] prop : item.properties )
                    {
                        DefinedPacket.writeString( prop[ 0], buf );
                        DefinedPacket.writeString( prop[ 1], buf );
                        if ( prop.length >= 3 )
                        {
                            buf.writeBoolean( true );
                            DefinedPacket.writeString( prop[ 2], buf );
                        } else
                        {
                            buf.writeBoolean( false );
                        }
                    }
                    DefinedPacket.writeVarInt( item.gamemode, buf );
                    DefinedPacket.writeVarInt( item.ping, buf );
                    buf.writeBoolean( item.displayName != null );
                    if ( item.displayName != null )
                    {
                        DefinedPacket.writeString( item.displayName, buf );
                    }
                    break;
                case UPDATE_GAMEMODE:
                    DefinedPacket.writeVarInt( item.gamemode, buf );
                    break;
                case UPDATE_LATENCY:
                    DefinedPacket.writeVarInt( item.ping, buf );
                    break;
                case UPDATE_DISPLAY_NAME:
                    buf.writeBoolean( item.displayName != null );
                    if ( item.displayName != null )
                    {
                        DefinedPacket.writeString( item.displayName, buf );
                    }
                    break;
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public static enum Action
    {

        ADD_PLAYER,
        UPDATE_GAMEMODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER;
    }

    @Data
    public static class Item
    {

        // ALL
        private UUID uuid;

        // ADD_PLAYER
        private String username;
        private String[][] properties;

        // ADD_PLAYER & UPDATE_GAMEMODE
        private int gamemode;

        // ADD_PLAYER & UPDATE_LATENCY
        private int ping;

        // ADD_PLAYER & UPDATE_DISPLAY_NAME
        private String displayName;

    }
}
