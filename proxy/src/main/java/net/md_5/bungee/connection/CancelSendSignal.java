package net.md_5.bungee.connection;

public class CancelSendSignal extends Error
{

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
