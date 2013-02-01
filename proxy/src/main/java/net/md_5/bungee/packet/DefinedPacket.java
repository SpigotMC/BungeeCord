package net.md_5.bungee.packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.Delegate;
import net.md_5.bungee.Util;

/**
 * This class represents a packet which has been given a special definition. All
 * subclasses can read and write to the backing byte array which can be
 * retrieved via the {@link #getPacket()} method.
 */
public abstract class DefinedPacket implements DataInput, DataOutput
{

    private interface Overriden
    {

        void readUTF();

        void writeUTF(String s);
    }
    @Delegate(excludes = Overriden.class)
    private ByteArrayDataInput in;
    @Delegate(excludes = Overriden.class)
    private ByteArrayDataOutput out;
    /**
     * Packet id.
     */
    public final int id;
    /**
     * Already constructed packet.
     */
    private byte[] packet;

    public DefinedPacket(int id, byte[] buf)
    {
        in = ByteStreams.newDataInput(buf);
        if (readUnsignedByte() != id)
        {
            throw new IllegalArgumentException("Wasn't expecting packet id " + Util.hex(id));
        }
        this.id = id;
        packet = buf;
    }

    public DefinedPacket(int id)
    {
        out = ByteStreams.newDataOutput();
        this.id = id;
        writeByte(id);
    }

    /**
     * Gets the bytes that make up this packet.
     *
     * @return the bytes which make up this packet, either the original byte
     * array or the newly written one.
     */
    public byte[] getPacket()
    {
        return packet == null ? out.toByteArray() : packet;
    }

    @Override
    public void writeUTF(String s)
    {
        writeShort(s.length());
        writeChars(s);
    }

    @Override
    public String readUTF()
    {
        short len = readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++)
        {
            chars[i] = this.readChar();
        }
        return new String(chars);
    }

    public void writeArray(byte[] b)
    {
        writeShort(b.length);
        write(b);
    }

    public byte[] readArray()
    {
        short len = readShort();
        byte[] ret = new byte[len];
        readFully(ret);
        return ret;
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    public void handle(PacketHandler handler) throws Exception
    {
        handler.handle(this);
    }
    private static Class<? extends DefinedPacket>[] classes = new Class[256];

    public static DefinedPacket packet(byte[] buf)
    {
        int id = Util.getId(buf);
        Class<? extends DefinedPacket> clazz = classes[id];
        DefinedPacket ret = null;
        if (clazz != null)
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = clazz.getDeclaredConstructor(byte[].class);
                if (constructor != null)
                {
                    ret = constructor.newInstance(buf);
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex)
            {
            }
        }
        return ret;
    }

    static
    {
        classes[0x00] = Packet0KeepAlive.class;
        classes[0x01] = Packet1Login.class;
        classes[0x02] = Packet2Handshake.class;
        classes[0x03] = Packet3Chat.class;
        classes[0x09] = Packet9Respawn.class;
        classes[0xC9] = PacketC9PlayerListItem.class;
        classes[0xCD] = PacketCDClientStatus.class;
        classes[0xFA] = PacketFAPluginMessage.class;
        classes[0xFC] = PacketFCEncryptionResponse.class;
        classes[0xFE] = PacketFEPing.class;
        classes[0xFF] = PacketFFKick.class;
    }
}
