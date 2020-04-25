package ru.leymooo.botfilter.utils;

import io.netty.handler.codec.CorruptedFrameException;

public class FastCorruptedFrameException extends CorruptedFrameException
{

    public FastCorruptedFrameException(String message)
    {
        super( message );
    }

    @Override
    public Throwable initCause(Throwable cause)
    {
        return this;
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}
