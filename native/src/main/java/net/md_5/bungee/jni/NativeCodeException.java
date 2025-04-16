package net.md_5.bungee.jni;

public class NativeCodeException extends RuntimeException
{

    public NativeCodeException(String message, int reason)
    {
        super( message + " : " + reason );
    }

    public NativeCodeException(String message)
    {
        super( message );
    }

}
