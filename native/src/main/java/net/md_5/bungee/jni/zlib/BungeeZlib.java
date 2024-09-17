package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.util.zip.DataFormatException;

public interface BungeeZlib
{

    public static final int OUTPUT_BUFFER_SIZE = 8192;

    void init(boolean compress, int level);

    void free();

    void process(ByteBuf in, ByteBuf out) throws DataFormatException;

    /*
     * This indicates whether the input ByteBuf is allowed to be a CompositeByteBuf.
     * If you need access to a memory address, you should not allow composite buffers.
     */
    boolean allowComposite();
}
