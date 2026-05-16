package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaZlib implements BungeeZlib
{

    private final ByteBuffer buffer = ByteBuffer.allocate( 8192 );
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
        ByteBuffer[] buffers = in.nioBuffers();
        buffer.clear();

        if ( compress )
        {
            deflater.setInput( buffers[ 0 ] );
            deflater.finish();

            while ( !deflater.finished() )
            {
                int count = deflater.deflate( buffer );
                buffer.flip();

                out.writeBytes( buffer );
                buffer.flip();

                if ( count == 0 && deflater.needsInput() )
                {
                    deflater.setInput( buffers[ ++bufferIndex ] );
                }
            }

            deflater.reset();
        } else
        {
            int inLength = in.readableBytes();

            inflater.setInput( buffers[ 0 ] );

            while ( !inflater.finished() && inflater.getBytesRead() < inLength )
            {
                int count = inflater.inflate( buffer );
                buffer.flip();

                out.writeBytes( buffer );
                buffer.flip();

                if ( count == 0 && inflater.needsInput() )
                {
                    inflater.setInput( buffers[ ++bufferIndex ] );
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
