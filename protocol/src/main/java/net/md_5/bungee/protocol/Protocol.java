package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Constructor;
import net.md_5.bungee.protocol.packet.DefinedPacket;
import net.md_5.bungee.protocol.skip.PacketReader;

public interface Protocol
{

    PacketReader getSkipper();

    DefinedPacket read(short packetId, ByteBuf buf);

    OpCode[][] getOpCodes();

    Class<? extends DefinedPacket>[] getClasses();

    Constructor<? extends DefinedPacket>[] getConstructors();
}
