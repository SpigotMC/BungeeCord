package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketC9PlayerListItem extends DefinedPacket
{

    public String username;
    public boolean online;
    public int ping;

    public PacketC9PlayerListItem(ByteBuf buf)
    {
        super( 0xC9, buf );
        username = readString();
        online = readBoolean();
        ping = readShort();
    }

    public PacketC9PlayerListItem(String username, boolean online, int ping)
    {
        super( 0xC9 );
        writeString( username );
        writeBoolean( online );
        writeShort( ping );
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
