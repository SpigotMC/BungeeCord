package net.md_5.bungee.protocol;

import io.netty.handler.codec.DecoderException;

public class BadPacketException extends DecoderException
{

    public BadPacketException(String message)
    {
        super( message );
    }

    public BadPacketException(String message, Throwable cause)
    {
        super( message, cause );
    }
}
