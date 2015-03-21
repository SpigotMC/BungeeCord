package net.md_5.bungee.compress;

import net.md_5.bungee.jni.NativeCode;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.jni.zlib.JavaZlib;
import net.md_5.bungee.jni.zlib.NativeZlib;

public class CompressFactory
{

    public static final NativeCode<BungeeZlib> zlib = new NativeCode( "native-compress", JavaZlib.class, NativeZlib.class );
}
