package net.md_5.bungee.jni;

public class NativeCodeException extends RuntimeException
{

    public NativeCodeException(String message, int reason)
    {
        super( message + " : " + reason );
    }
}
