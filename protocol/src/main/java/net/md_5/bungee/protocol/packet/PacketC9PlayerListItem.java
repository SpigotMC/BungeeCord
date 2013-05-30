package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketC9PlayerListItem extends DefinedPacket
{

    private String username;
    private boolean online;
    private int ping;

    private PacketC9PlayerListItem()
    {
        super( 0xC9 );
    }

    public PacketC9PlayerListItem(String username, boolean online, int ping)
    {
        super( 0xC9 );
        this.username = username;
        this.online = online;
        this.ping = ping;
    }

    @Override
    public void read(ByteBuf buf)
    {
        username = readString( buf );
        online = buf.readBoolean();
        ping = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( username, buf );
        buf.writeBoolean( online );
        buf.writeInt( ping );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
