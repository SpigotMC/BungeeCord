package net.md_5.bungee.jni.zlib;

public class NativeCompressImpl {

    static {
        initFields();
    }

    int consumed;
    boolean finished;

    static native void initFields();

    native void end(long ctx, boolean compress);

    native void reset(long ctx, boolean compress);

    native long init(boolean compress, int compressionLevel);

    native int process(long ctx, long in, int inLength, long out, int outLength, boolean compress);
}
