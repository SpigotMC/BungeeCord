package net.md_5.bungee.protocol.packet.game;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.ProtocolConstants;

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
        action = Action.values()[readVarInt( buf )];
        items = new Item[ readVarInt( buf ) ];
        for ( int i = 0; i < items.length; i++ )
        {
            Item item = items[i] = new Item();
            item.setUuid( readUUID( buf ) );
            switch ( action )
            {
                case ADD_PLAYER:
                    item.username = readString( buf );
                    item.properties = readProperties( buf );
                    item.gamemode = readVarInt( buf );
                    item.ping = readVarInt( buf );
                    if ( buf.readBoolean() )
                    {
                        item.displayName = readBaseComponent( buf, protocolVersion );
                    }
                    if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
                    {
                        item.publicKey = readPublicKey( buf );
                    }
                    break;
                case UPDATE_GAMEMODE:
                    item.gamemode = readVarInt( buf );
                    break;
                case UPDATE_LATENCY:
                    item.ping = readVarInt( buf );
                    break;
                case UPDATE_DISPLAY_NAME:
                    if ( buf.readBoolean() )
                    {
                        item.displayName = readBaseComponent( buf, protocolVersion );
                    }
            }
        }
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion)
    {
        writeVarInt( action.ordinal(), buf );
        writeVarInt( items.length, buf );
        for ( Item item : items )
        {
            writeUUID( item.uuid, buf );
            switch ( action )
            {
                case ADD_PLAYER:
                    writeString( item.username, buf );
                    writeProperties( item.properties, buf );
                    writeVarInt( item.gamemode, buf );
                    writeVarInt( item.ping, buf );
                    buf.writeBoolean( item.displayName != null );
                    if ( item.displayName != null )
                    {
                        writeBaseComponent( item.displayName, buf, protocolVersion );
                    }
                    if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_19 )
                    {
                        writePublicKey( item.publicKey, buf );
                    }
                    break;
                case UPDATE_GAMEMODE:
                    writeVarInt( item.gamemode, buf );
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
        UUID uuid;

        // ADD_PLAYER
        String username;
        Property[] properties;

        UUID chatSessionId;
        PlayerPublicKey publicKey;

        // UPDATE_LISTED
        Boolean listed;

        // ADD_PLAYER & UPDATE_GAMEMODE
        Integer gamemode;

        // ADD_PLAYER & UPDATE_LATENCY
        Integer ping;

        // ADD_PLAYER & UPDATE_DISPLAY_NAME
        BaseComponent displayName;

    }
}
