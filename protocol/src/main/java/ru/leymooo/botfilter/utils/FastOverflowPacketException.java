package ru.leymooo.botfilter.utils;

import net.md_5.bungee.protocol.OverflowPacketException;

public class FastOverflowPacketException extends OverflowPacketException
{

    public FastOverflowPacketException(String message)
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
