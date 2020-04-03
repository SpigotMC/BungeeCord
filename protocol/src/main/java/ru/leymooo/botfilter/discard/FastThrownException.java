package ru.leymooo.botfilter.discard;

public class FastThrownException extends RuntimeException
{

    public FastThrownException(String message)
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
