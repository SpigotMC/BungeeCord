package net.md_5.bungee.jni.zlib;

import net.md_5.bungee.jni.NativeCodeException;

public class NativeCompressImpl
{

    int consumed;
    boolean finished;

    static
    {
        initFields();
    }

    static native void initFields();

    native boolean checkSupported();

    native void end(long ctx, boolean compress);

    native void reset(long ctx, boolean compress);

    native long init(boolean compress, int compressionLevel);

    native int process(long ctx, long in, int inLength, long out, int outLength, boolean compress);

    NativeCodeException makeException(String message, int err)
    {
        return new NativeCodeException( message, err );
    }
}
