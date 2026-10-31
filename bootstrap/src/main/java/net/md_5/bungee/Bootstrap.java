package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;

public class Bootstrap
{

    public static void main(String[] args) throws Exception
    {
        String defaultMainClass = resource2Lines( "main-class" ).get( 0 );
        String mainClassName = System.getProperty( "bundlerMainClass", defaultMainClass );

        Path repoPath = Paths.get( System.getProperty( "bundlerRepoDir", "bundler" ) ).toAbsolutePath();
        Files.createDirectories( repoPath );

        List<URL> urls = new ArrayList<>();
        boolean readOnly = Boolean.getBoolean( "bundlerReadOnly" );
        // Everything in libraries for now
        boolean success = extractFromIndex( "libraries", repoPath, urls, readOnly );
        if ( !success )
        {
            System.err.println( "Could not extract required files" );
            System.exit( 1 );
        }

        if ( mainClassName.isEmpty() )
        {
            System.out.println( "No main class specified" );
            System.exit( 0 );
        }

        URLClassLoader classLoader = new URLClassLoader( urls.toArray( URL[]::new ) );

        System.out.println( "Launching " + mainClassName );
        Thread thread = new Thread( new Runnable()
        {
            @Override
            @SneakyThrows
            public void run()
            {
                Class<?> mainClass = Class.forName( mainClassName, true, classLoader );
                MethodHandle mainMethod = MethodHandles.lookup().findStatic( mainClass, "main", MethodType.methodType( void.class, String[].class ) ).asFixedArity();
                mainMethod.invoke( args );
            }
        }, "Main Thread" );

        thread.setContextClassLoader( classLoader );
        thread.start();
    }

    private static List<String> resource2Lines(String name) throws Exception
    {
        try ( InputStream is = Bootstrap.class.getResourceAsStream( "/META-INF/" + name ) )
        {
            if ( is == null )
            {
                throw new IllegalStateException( "Resource " + name + " not found" );
            }

            return new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) ).lines().toList();
        }
    }

    private static boolean extractFromIndex(String type, Path repoDir, List<URL> extractedUrls, boolean readOnly) throws Exception
    {
        boolean failed = false;
        Path outputDir = repoDir.resolve( type );
        System.out.printf( "Unbundling %s to %s%n", type, outputDir );

        for ( String line : resource2Lines( type + ".list" ) )
        {
            HashEntry hashEntry = HashEntry.fromString( line );
            if ( hashEntry.file.startsWith( "bungeecord-module-" ) )
            {
                continue;
            }

            Path extractedFile = extract( type, outputDir, hashEntry, readOnly );
            if ( extractedFile != null )
            {
                extractedUrls.add( extractedFile.toUri().toURL() );
            } else
            {
                failed = true;
            }
        }

        return !failed;
    }

    private static Path extract(String type, Path outputDir, HashEntry entry, boolean readOnly) throws Exception
    {
        Path outputFile = outputDir.resolve( entry.file );
        if ( !Files.exists( outputFile ) || !checkHash( outputFile, entry.hash ) )
        {
            if ( readOnly )
            {
                System.err.printf( "Read-only, cannot extract %s (%s) to %s%n", entry.file, type, outputFile );
                return null;
            }

            System.out.printf( "Extracting %s/%s to %s%n", type, entry.file, outputFile );
            extractFile( type, entry.file, outputFile );
        }

        return outputFile;
    }

    private static boolean checkHash(Path file, String expectedHash) throws Exception
    {
        String fileHash = getHash( file );
        if ( !fileHash.equalsIgnoreCase( expectedHash ) )
        {
            System.out.printf( "Expected file %s to have hash %s, but got %s%n", file, expectedHash, fileHash );
            return false;
        }

        return true;
    }

    private static String getHash(Path file) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance( "SHA-256" );

        try ( InputStream output = Files.newInputStream( file ) )
        {
            output.transferTo( new DigestOutputStream( OutputStream.nullOutputStream(), digest ) );

            return HexFormat.of().formatHex( digest.digest() );
        }
    }

    private static void extractFile(String type, String file, Path output) throws IOException
    {
        Files.createDirectories( output.getParent() );

        try ( InputStream is = Bootstrap.class.getResourceAsStream( "/META-INF/" + type + "/" + file ) )
        {
            if ( is == null )
            {
                throw new IllegalStateException( "Required file " + file + " not found" );
            }

            Files.copy( is, output, StandardCopyOption.REPLACE_EXISTING );
        }
    }

    @Data
    private static final class HashEntry
    {

        private final String hash;
        private final String file;

        public static HashEntry fromString(String string)
        {
            String[] split = string.split( " " );
            if ( split.length != 2 || !split[1].startsWith( "*" ) )
            {
                throw new IllegalStateException( "Invalid hash summary: " + string );
            }

            String path = split[1].substring( 1 );
            return new HashEntry( split[0], path );
        }
    }
}
