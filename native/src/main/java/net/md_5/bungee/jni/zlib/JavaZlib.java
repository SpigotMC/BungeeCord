package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaZlib implements BungeeZlib
{

    private final byte[] buffer = new byte[ 8192 ];
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
        byte[] inData = new byte[ in.readableBytes() ];
        in.readBytes( inData );

        if ( compress )
        {
            deflater.setInput( inData );
            deflater.finish();

            while ( !deflater.finished() )
            {
                int count = deflater.deflate( buffer );
                out.writeBytes( buffer, 0, count );
            }

            deflater.reset();
        } else
        {
            inflater.setInput( inData );

            while ( !inflater.finished() && inflater.getTotalIn() < inData.length )
            {
                int count = inflater.inflate( buffer );
                out.writeBytes( buffer, 0, count );
            }

            inflater.reset();
        }
    }

    @Override
    public void process(ByteBuf in, ByteBuf out, boolean preallocatedBuffer) throws DataFormatException
    {
        process( in, out );
    }
}
