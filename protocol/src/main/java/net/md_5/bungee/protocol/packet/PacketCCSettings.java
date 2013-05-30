package net.md_5.bungee.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.packet.PacketHandler;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PacketCCSettings extends DefinedPacket
{

    public String locale;
    public byte viewDistance;
    public byte chatFlags;
    public byte difficulty;
    public boolean showCape;

    public PacketCCSettings(byte[] buf)
    {
        super( 0xCC, buf );
        locale = readUTF();
        viewDistance = readByte();
        chatFlags = readByte();
        difficulty = readByte();
        showCape = readBoolean();
    }

    @Override
    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
