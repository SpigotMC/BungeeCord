package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet51MapChunk extends Packet {

    @Override
    public void a(DataInputStream datainputstream) throws IOException {
        datainputstream.readInt();
        datainputstream.readInt();
        datainputstream.readBoolean();
        datainputstream.readShort();
        datainputstream.readShort();
        int size = datainputstream.readInt();
        byte[] buf = new byte[size];
        datainputstream.readFully(buf, 0, size);
    }

    @Override
    public void a(DataOutputStream dataoutputstream) throws IOException {
    }

    @Override
    public void handle(NetHandler nethandler) {
    }

    @Override
    public int a() {
        return 0;
    }
}
