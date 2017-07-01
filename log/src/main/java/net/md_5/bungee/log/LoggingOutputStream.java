package net.md_5.bungee.log;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class LoggingOutputStream extends ByteArrayOutputStream
{

    private static final String separator = System.getProperty( "line.separator" );
    /*========================================================================*/
    private final Logger logger;
    private final Level level;

    @Override
    public void flush() throws IOException
    {
        String contents = toString( Charsets.UTF_8.name() );
        super.reset();
        if ( !contents.isEmpty() && !contents.equals( separator ) )
        {
            logger.log( level, contents );
        }
    }
}
