package net.md_5.bungee.connection;

class CancelSendSignal extends Error
{

    @Override
    public synchronized Throwable initCause(Throwable cause)
    {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
