package net.md_5.bungee.jni.zlib;

public class NativeCompressImpl
{

	/* default */ int consumed;
	/* default */ boolean finished;

    static
    {
        initFields();
    }

    /* default */ static native void initFields();

    /* default */ native void end(long ctx, boolean compress);

    /* default */ native void reset(long ctx, boolean compress);

    /* default */ native long init(boolean compress, int compressionLevel);

    /* default */ native int process(long ctx, long in, int inLength, long out, int outLength, boolean compress);
}
