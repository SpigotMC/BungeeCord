package ru.leymooo.botfilter.discard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorStream {
	private static final BufferedWriter WRITER = new BufferedWriter( new OutputStreamWriter( System.err, StandardCharsets.UTF_8 ) );

	public void error(String message)
	{
		try
		{
			WRITER.write( message );
			WRITER.newLine();
		} catch ( IOException ex )
		{
			// That should not happen.
			ex.printStackTrace( System.out );
		}
	}

	public void init() { } // Just to initialize the class
}
