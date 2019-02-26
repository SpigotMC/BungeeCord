package net.md_5.bungee.util;

import java.lang.Thread;

public class ShutdownThread extends Thread
{
    public String reason;

    public ShutdownThread(String name)
    {
        super( null, null, name );
    }

    public ShutdownThread bind(String reason)
    {
        this.reason = reason;
        return this;
    }
}