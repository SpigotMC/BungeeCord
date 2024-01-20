package net.md_5.bungee.protocol.packet.game;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.game.PlayerListItem.Item;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PlayerListItemUpdate extends DefinedPacket
{

    private EnumSet<Action> actions;
    private Item[] items;

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        actions = readEnumSet( PlayerListItemUpdate.Action.class, buf );

        items = new Item[ readVarInt( buf ) ];
        for ( int i = 0; i < items.length; i++ )
        {
            Item item = items[i] = new Item();
            item.setUuid( readUUID( buf ) );

            for ( Action action : actions )
            {
                switch ( action )
                {
                    case ADD_PLAYER:
                        item.username = readString( buf );
                        item.properties = readProperties( buf );
                        break;
                    case INITIALIZE_CHAT:
                        if ( buf.readBoolean() )
                        {
                            item.chatSessionId = readUUID( buf );
                            item.publicKey = new PlayerPublicKey( buf.readLong(), readArray( buf, 512 ), readArray( buf, 4096 ) );
                        }
                        break;
                    case UPDATE_GAMEMODE:
                        item.gamemode = readVarInt( buf );
                        break;
                    case UPDATE_LISTED:
                        item.listed = buf.readBoolean();
                        break;
                    case UPDATE_LATENCY:
                        item.ping = readVarInt( buf );
                        break;
                    case UPDATE_DISPLAY_NAME:
                        if ( buf.readBoolean() )
                        {
                            item.displayName = readBaseComponent( buf, protocolVersion );
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeEnumSet( actions, PlayerListItemUpdate.Action.class, buf );

        writeVarInt( items.length, buf );
        for ( Item item : items )
        {
            writeUUID( item.uuid, buf );
            for ( Action action : actions )
            {
                switch ( action )
                {
                    case ADD_PLAYER:
                        writeString( item.username, buf );
                        writeProperties( item.properties, buf );
                        break;
                    case INITIALIZE_CHAT:
                        buf.writeBoolean( item.chatSessionId != null );
                        if ( item.chatSessionId != null )
                        {
                            writeUUID( item.chatSessionId, buf );
                            buf.writeLong( item.publicKey.getExpiry() );
                            writeArray( item.publicKey.getKey(), buf );
                            writeArray( item.publicKey.getSignature(), buf );
                        }
                        break;
                    case UPDATE_GAMEMODE:
                        writeVarInt( item.gamemode, buf );
                        break;
                    case UPDATE_LISTED:
                        buf.writeBoolean( item.listed );
                        break;
                    case UPDATE_LATENCY:
                        writeVarInt( item.ping, buf );
                        break;
                    case UPDATE_DISPLAY_NAME:
                        buf.writeBoolean( item.displayName != null );
                        if ( item.displayName != null )
                        {
                            writeBaseComponent( item.displayName, buf, protocolVersion );
                        }
                        break;
                }
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
        INITIALIZE_CHAT,
        UPDATE_GAMEMODE,
        UPDATE_LISTED,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME;
    }
}
