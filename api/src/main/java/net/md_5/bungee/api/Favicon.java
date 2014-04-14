package net.md_5.bungee.api;

import com.google.common.io.BaseEncoding;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Favicon shown in the server list.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Favicon
{
    private static final TypeAdapter<Favicon> FAVICON_TYPE_ADAPTER = new TypeAdapter<Favicon>()
    {
        @Override
        public void write(JsonWriter out, Favicon value) throws IOException
        {
            TypeAdapters.STRING.write( out, value.getEncoded() );
        }

        @Override
        public Favicon read(JsonReader in) throws IOException
        {
            return create( TypeAdapters.STRING.read( in ) );
        }
    };

    public static TypeAdapter<Favicon> getFaviconTypeAdapter()
    {
        return FAVICON_TYPE_ADAPTER;
    }

    /**
     * The encoded favicon, including MIME header.
     */
    @Getter
    private final String encoded;

    /**
     * Reads a favicon from a file.
     *
     * @see #create(java.awt.image.BufferedImage)
     */
    public static Favicon read(File file) throws IOException
    {
        return read( file.toPath() );
    }

    /**
     * Reads a favicon from a file.
     *
     * @see #create(java.awt.image.BufferedImage)
     */
    public static Favicon read(Path file) throws IOException
    {
        try ( InputStream stream = Files.newInputStream( file ) )
        {
            return read( stream );
        }
    }

    /**
     * Reads a favicon from a stream.
     *
     * @see #create(java.awt.image.BufferedImage)
     */
    public static Favicon read(InputStream source) throws IOException
    {
        BufferedImage image = ImageIO.read( source );
        return create( image );
    }

    /**
     * Creates a Favicon from an image.
     * <p/>
     * Currently, this image must be exactly 64 * 64 pixel in size.
     */
    public static Favicon create(BufferedImage image)
    {
        // check size
        if ( image.getWidth() != 64 || image.getHeight() != 64 )
        {
            throw new IllegalArgumentException( "Server icon must be exactly 64x64 pixels" );
        }

        // dump image PNG
        byte[] imageBytes;
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write( image, "PNG", stream );
            imageBytes = stream.toByteArray();
        } catch ( IOException e )
        {
            // ByteArrayOutputStream should never throw this
            throw new AssertionError( e );
        }

        // encode with header
        String encoded = "data:image/png;base64," + BaseEncoding.base64().encode( imageBytes );

        // check encoded image size
        if ( encoded.length() > Short.MAX_VALUE )
        {
            throw new IllegalArgumentException( "Favicon file too large for server to process" );
        }

        // create
        return create( encoded );
    }

    /**
     * Creates a Favicon from an encoded PNG.
     *
     * @deprecated Use #create(java.awt.image.BufferedImage) or one of the read methods instead.
     */
    @Deprecated
    public static Favicon create(String encodedString)
    {
        return new Favicon( encodedString );
    }
}
