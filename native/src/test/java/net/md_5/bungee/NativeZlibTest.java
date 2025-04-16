package net.md_5.bungee;

import static org.junit.jupiter.api.Assertions.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import net.md_5.bungee.jni.NativeCode;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.jni.zlib.JavaZlib;
import net.md_5.bungee.jni.zlib.NativeZlib;
import org.junit.jupiter.api.Test;

public class NativeZlibTest
{

    private final NativeCode<BungeeZlib> factory = new NativeCode<>( "native-compress", JavaZlib::new, NativeZlib::new, true );

    @Test
    public void doTest() throws DataFormatException
    {
        if ( NativeCode.isSupported() )
        {
            assertTrue( factory.load(), "Native code failed to load!" );
            test( factory.newInstance() );
        }
        test( new JavaZlib() );
    }

    @Test
    public void testException() throws DataFormatException
    {
        if ( NativeCode.isSupported() )
        {
            assertTrue( factory.load(), "Native code failed to load!" );
            testExceptionImpl( factory.newInstance() );
        }
        testExceptionImpl( new JavaZlib() );
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

        ByteBuf uncompressed = Unpooled.directBuffer( dataBuf.length, dataBuf.length );

        zlib.init( false, 0 );
        zlib.process( compressed, uncompressed );

        byte[] check = new byte[ uncompressed.readableBytes() ];
        uncompressed.readBytes( check );

        long elapsed = System.currentTimeMillis() - start;
        System.out.println( "Took: " + elapsed + "ms" );

        assertTrue( Arrays.equals( dataBuf, check ), "Results do not match" );
    }

    private void testExceptionImpl(BungeeZlib zlib) throws DataFormatException
    {
        System.out.println( "Testing Exception: " + zlib );
        long start = System.currentTimeMillis();

        byte[] dataBuf = new byte[ 1 << 12 ]; // 4096 random bytes
        new Random().nextBytes( dataBuf );

        zlib.init( false, 0 );

        ByteBuf originalBuf = Unpooled.directBuffer();
        originalBuf.writeBytes( dataBuf );

        ByteBuf decompressed = Unpooled.directBuffer();

        assertThrows( DataFormatException.class, () -> zlib.process( originalBuf, decompressed ), "Decompressing random bytes did not result in a DataFormatException!" );
    }
}
