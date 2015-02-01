package net.md_5.bungee.jni.cipher;

class NativeCipherImpl
{

    native long init(boolean forEncryption, byte[] key);

    native void free(long ctx);

    native void cipher(long ctx, long in, long out, int length);
}
