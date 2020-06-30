package net.md_5.bungee.error;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@UtilityClass
public class Errors
{
    private final boolean DEBUG = Boolean.getBoolean( "net.md_5.bungee.debug" );
    private final QuietException INVALID_PROTOCOL = newError( "Invalid protocol requested" );
    private final QuietException UNEXPECTED_LOGIN_PACKET = newError( "Invalid packet received during login process" );
    private final QuietException VARINT_TOO_BIG = newError( "VarInt too big" );
    private final QuietException UNEXPECTED_BUFFER_END = newError( "Buffer end reached unexpectedly" );
    private final QuietException BAD_FRAME_LENGTH = newError( "length wider than 21-bit" );
    private final QuietException EMPTY_PACKET = newError( "Empty paket" );
    private final QuietException DISCARD = newError( "Discard handler is added to the pipeline" );

    public void invalidProtocol(int protocol)
    {
        if ( DEBUG )
        {
            throw new IllegalStateException( "Cannot request protocol " + protocol );
        }
        throw INVALID_PROTOCOL;
    }

    public void unexpectedLoginPacket(ByteBuf raw)
    {
        if ( DEBUG )
        {
            throw new IllegalStateException( "Unexpected packet received during login process! " + BufUtil.dump( raw, 16 ) );
        }
        throw UNEXPECTED_LOGIN_PACKET;
    }

    public void varIntTooBig()
    {
        if ( DEBUG )
        {
            throw new IllegalStateException( "VarInt too big" );
        }
        throw VARINT_TOO_BIG;
    }

    public void endOfBuffer()
    {
        if ( DEBUG )
        {
            throw new IllegalStateException( "Buffer end reached unexpectedly" );
        }
        throw UNEXPECTED_BUFFER_END;
    }

    public void badFrameLength()
    {
        if ( DEBUG )
        {
            throw new CorruptedFrameException( "length wider than 21-bit" );
        }
        throw BAD_FRAME_LENGTH;
    }

    public void emptyPacket()
    {
        if ( DEBUG )
        {
            throw new CorruptedFrameException( "Empty Packet!" );
        }
        throw EMPTY_PACKET;
    }

    public Exception discard()
    {
        if ( DEBUG )
        {
            return new IllegalStateException( "Discard handler is added to the pipeline" );
        }
        return DISCARD;
    }

    public boolean isDebug()
    {
        return DEBUG;
    }

    private QuietException newError(String message)
    {
        return new QuietException( message + " ; Enable debugging via -Dnet.md_5.bungee.debug=true for more info." );
    }
}
