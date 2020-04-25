package ru.leymooo.botfilter.utils;

import net.md_5.bungee.protocol.BadPacketException;

public class FastBadPacketException extends BadPacketException
{

    public FastBadPacketException(String message)
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
