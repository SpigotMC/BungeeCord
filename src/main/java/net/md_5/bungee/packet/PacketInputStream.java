package net.md_5.bungee.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import net.md_5.bungee.Util;
import net.minecraft.server.Packet;

/**
 * A specialized input stream to parse packets using the Mojang packet
 * definitions and then return them as a byte array.
 */
public class PacketInputStream
{

    private final DataInputStream dataInput;
    private final TrackingInputStream tracker;

    public PacketInputStream(InputStream in)
    {
        tracker = new TrackingInputStream(in);
        dataInput = new DataInputStream(tracker);
    }

    /**
     * Read an entire packet from the stream and return it as a byte array.
     *
     * @return the read packet
     * @throws IOException when the underlying input stream throws an exception
     */
    public byte[] readPacket() throws IOException
    {
        tracker.out.reset();
        int id = tracker.read();
        if (id == -1)
        {
            throw new EOFException();
        }
        Packet codec = VanillaPackets.packets[id];
        if (codec == null)
        {
            throw new RuntimeException("No Packet id: " + Util.hex(id));
        }
        codec.a(dataInput);
        return tracker.out.toByteArray();

    }

    /**
     * Input stream which will wrap another stream and copy all bytes read to a
     * {@link ByteArrayOutputStream}.
     */
    private class TrackingInputStream extends InputStream
    {

        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final InputStream wrapped;

        public TrackingInputStream(InputStream wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException
        {
            int ret = wrapped.read();
            out.write(ret);
            return ret;
        }
    }
}
