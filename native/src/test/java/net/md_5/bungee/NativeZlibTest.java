package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import net.md_5.bungee.jni.NativeCode;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.jni.zlib.JavaZlib;
import net.md_5.bungee.jni.zlib.NativeZlib;
import org.junit.Assert;
import org.junit.Test;

public class NativeZlibTest
{

    private final NativeCode<BungeeZlib> factory = new NativeCode<>( "native-compress", JavaZlib.class, NativeZlib.class );

    @Test
    public void doTest() throws DataFormatException
    {
        if ( NativeCode.isSupported() )
        {
            Assert.assertTrue( "Native code failed to load!", factory.load() );
            test( factory.newInstance() );
        }
        test( new JavaZlib() );
    }

    private void test(BungeeZlib zlib) throws DataFormatException
    {
        System.out.println( "Testing: " + zlib );
        long start = System.currentTimeMillis();

        byte[] dataBuf = new byte[ 1 << 22 ]; // 2 megabytes
        new Random().nextBytes( dataBuf );

        zlib.init( true, 9 );

        ByteBuf originalBuf = Unpooled.directBuffer();
        originalBuf.writeBytes( dataBuf );

        ByteBuf compressed = Unpooled.directBuffer();

        zlib.process( originalBuf, compressed );

        // Repeat here to test .reset()
        originalBuf = Unpooled.directBuffer();
        originalBuf.writeBytes( dataBuf );

        compressed = Unpooled.directBuffer();

        zlib.process( originalBuf, compressed );

        ByteBuf uncompressed = Unpooled.directBuffer();

        zlib.init( false, 0 );
        zlib.process( compressed, uncompressed );

        byte[] check = new byte[ uncompressed.readableBytes() ];
        uncompressed.readBytes( check );

        long elapsed = System.currentTimeMillis() - start;
        System.out.println( "Took: " + elapsed + "ms" );

        Assert.assertTrue( "Results do not match", Arrays.equals( dataBuf, check ) );
    }
}
