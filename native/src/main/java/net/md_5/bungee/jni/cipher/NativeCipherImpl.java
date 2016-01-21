package net.md_5.bungee.jni.cipher;

class NativeCipherImpl
{

	/* default */ native long init(boolean forEncryption, byte[] key);

	/* default */ native void free(long ctx);

	/* default */ native void cipher(long ctx, long in, long out, int length);
}
