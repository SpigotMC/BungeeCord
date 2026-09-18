package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaZlib implements BungeeZlib
{

    private final ByteBuffer bufferOut = ByteBuffer.allocate( 8192 );
    //
    private boolean compress;
    private Deflater deflater;
    private Inflater inflater;

    @Override
    public void init(boolean compress, int level)
    {
        this.compress = compress;
        free();

        if ( compress )
        {
            deflater = new Deflater( level );
        } else
        {
            inflater = new Inflater();
        }
    }

    @Override
    public void free()
    {
        bufferOut.clear();

        if ( deflater != null )
        {
            deflater.end();
        }
        if ( inflater != null )
        {
            inflater.end();
        }
    }

    @Override
    public void process(ByteBuf in, ByteBuf out) throws DataFormatException
    {
        int bufferIndex = 0;
        ByteBuffer[] buffersIn = in.nioBuffers();
        bufferOut.clear();

        if ( compress )
        {
            deflater.setInput( buffersIn[ bufferIndex ] );
            deflater.finish();

            while ( !deflater.finished() )
            {
                int count = deflater.deflate( bufferOut );
                bufferOut.flip();

                out.writeBytes( bufferOut );
                bufferOut.flip();

                if ( count == 0 && deflater.needsInput() )
                {
                    deflater.setInput( buffersIn[ ++bufferIndex ] );
                }
            }

            deflater.reset();
        } else
        {
            int readableBytes = in.readableBytes();

            inflater.setInput( buffersIn[ 0 ] );

            while ( !inflater.finished() && inflater.getBytesRead() < readableBytes )
            {
                int count = inflater.inflate( bufferOut );
                bufferOut.flip();

                out.writeBytes( bufferOut );
                bufferOut.flip();

                if ( count == 0 && inflater.needsInput() )
                {
                    inflater.setInput( buffersIn[ ++bufferIndex ] );
                }
            }

            inflater.reset();
        }
    }

    @Override
    public boolean allowComposite()
    {
        return true;
    }
}
