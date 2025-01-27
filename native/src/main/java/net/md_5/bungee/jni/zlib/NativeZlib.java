package net.md_5.bungee.jni.zlib;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.zip.DataFormatException;
import lombok.Getter;
import net.md_5.bungee.jni.NativeCodeException;

public class NativeZlib implements BungeeZlib
{

    @Getter
    private final NativeCompressImpl nativeCompress = new NativeCompressImpl();
    /*============================================================================*/
    private boolean compress;
    private long ctx;

    public NativeZlib()
    {
        if ( !nativeCompress.checkSupported() )
        {
            throw new NativeCodeException( "This CPU does not support the required SSE 4.2 and/or PCLMUL extensions!" );
        }
    }

    @Override
    public void init(boolean compress, int level)
    {
        free();

        this.compress = compress;
        this.ctx = nativeCompress.init( compress, level );
    }

    @Override
    public void free()
    {
        if ( ctx != 0 )
        {
            nativeCompress.end( ctx, compress );
            ctx = 0;
        }

        nativeCompress.consumed = 0;
        nativeCompress.finished = false;
    }

    @Override
    public void process(ByteBuf in, ByteBuf out) throws DataFormatException
    {
        Preconditions.checkState( ctx != 0, "Invalid pointer to compress!" );

        while ( !nativeCompress.finished && ( compress || in.isReadable() ) )
        {
            if ( compress )
            {
                out.ensureWritable( OUTPUT_BUFFER_SIZE );
            } else
            {
                Preconditions.checkArgument( out.isWritable(), "Output buffer overrun" );
            }

            int processed;
            try
            {
                processed = nativeCompress.process( ctx, in.memoryAddress() + in.readerIndex(), in.readableBytes(), out.memoryAddress() + out.writerIndex(), out.writableBytes(), compress );
            } catch ( NativeCodeException exception )
            {
                throw (DataFormatException) new DataFormatException( "Failed to decompress via Zlib!" ).initCause( exception );
            }

            in.readerIndex( in.readerIndex() + nativeCompress.consumed );
            out.writerIndex( out.writerIndex() + processed );
        }

        nativeCompress.reset( ctx, compress );
        nativeCompress.consumed = 0;
        nativeCompress.finished = false;
    }

    @Override
    public boolean allowComposite()
    {
        return false;
    }
}
