package net.md_5.bungee.util;

/**
 * Exception without a stack trace component.
 */
public class QuietException extends RuntimeException
{

    public QuietException(String message)
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

    /**
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     * string using {@link String#valueOf(Object)}
     * @throws net.md_5.bungee.util.QuietException if {@code expression} is false
     * @see com.google.common.base.Preconditions#checkState(boolean, Object)
     */
    public static void checkState(boolean expression, String errorMessage)
    {
        if ( !expression )
        {
            throw new QuietException( errorMessage );
        }
    }
}
