package net.md_5.bungee.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import net.md_5.bungee.Util;
import net.minecraft.server.Packet;

public class PacketInputStream {

    private final DataInputStream dataInput;
    private final TrackingInputStream tracker;

    public PacketInputStream(InputStream in) {
        tracker = new TrackingInputStream(in);
        dataInput = new DataInputStream(tracker);
    }

    public byte[] readPacket() throws IOException {
        tracker.out.reset();
        int id = tracker.read();
        if (id == -1) {
            throw new EOFException();
        }
        Packet codec = VanillaPackets.packets[id];
        if (codec == null) {
            throw new RuntimeException("No Packet id: " + Util.hex(id));
        }
        codec.a(dataInput);
        return tracker.out.toByteArray();

    }

    private class TrackingInputStream extends InputStream {

        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final InputStream wrapped;

        public TrackingInputStream(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int read() throws IOException {
            int ret = wrapped.read();
            out.write(ret);
            return ret;
        }
    }
}
