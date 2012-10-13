package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

    public boolean lowPriority = false;

    public static void a(DataOutputStream dataoutputstream, byte[] abyte) throws IOException {
        dataoutputstream.writeShort(abyte.length);
        dataoutputstream.write(abyte);
    }

    public static byte[] b(DataInputStream datainputstream) throws IOException {
        short short1 = datainputstream.readShort();

        if (short1 < 0) {
            throw new IOException("Key was smaller than nothing!  Weird key!");
        } else {
            byte[] abyte = new byte[short1];

            datainputstream.read(abyte);
            return abyte;
        }
    }

    public static String a(DataInputStream datainputstream, int i) throws IOException {
        short short1 = datainputstream.readShort();

        if (short1 < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        } else {
            StringBuilder stringbuilder = new StringBuilder();

            for (int j = 0; j < short1; ++j) {
                stringbuilder.append(datainputstream.readChar());
            }

            return stringbuilder.toString();
        }
    }

    public abstract void a(DataInputStream datainputstream);

    public abstract void a(DataOutputStream dataoutputstream);

    public abstract void handle(NetHandler nethandler);

    public abstract int a();

    public static ItemStack c(DataInputStream datainputstream) throws IOException {
        ItemStack itemstack = null;
        short short1 = datainputstream.readShort();

        if (short1 >= 0) {
            byte b0 = datainputstream.readByte();
            short short2 = datainputstream.readShort();

            itemstack = new ItemStack(short1, b0, short2);
            itemstack.tag = d(datainputstream);
        }

        return itemstack;
    }

    public static NBTTagCompound d(DataInputStream datainputstream) throws IOException {
        short short1 = datainputstream.readShort();

        if (short1 < 0) {
            return null;
        } else {
            byte[] abyte = new byte[short1];

            datainputstream.readFully(abyte);
            return NBTCompressedStreamTools.a(abyte);
        }
    }
}
