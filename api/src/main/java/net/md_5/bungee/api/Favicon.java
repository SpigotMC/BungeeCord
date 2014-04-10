package net.md_5.bungee.api;

import com.google.common.io.BaseEncoding;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author TheJeterLP
 */
public class Favicon
{

    private String favicon = "";

    public Favicon(File fav) throws FaviconException
    {
        if ( fav == null )
        {
            return;
        }
        try
        {
            if ( !fav.exists() )
            {
                throw new FaviconException( "Could not load server icon because it does not exist!" );
            }
            if ( !fav.isFile() )
            {
                throw new FaviconException( "Could not load server icon because it is not a file!" );
            }
            if ( !fav.getName().toLowerCase().endsWith( ".png" ) )
            {
                throw new FaviconException( "Could not load server icon because it is not a PNG file!" );
            }
            BufferedImage image = ImageIO.read( fav );
            if ( image != null )
            {
                if ( image.getHeight() == 64 && image.getWidth() == 64 )
                {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ImageIO.write( image, "png", bytes );
                    favicon = "data:image/png;base64," + BaseEncoding.base64().encode( bytes.toByteArray() );
                    if ( favicon.length() > Short.MAX_VALUE )
                    {
                        throw new FaviconException( "Favicon file too large for server to process" );
                    }
                } else
                {
                    throw new FaviconException( "Server icon must be exactly 64x64 pixels" );
                }
            } else

            {
                throw new FaviconException( "Could not load server icon for unknown reason. Please double check its format." );
            }
        } catch ( IOException e )
        {
            throw new FaviconException( e.getMessage() );
        }

    }

    /**
     * Gets the string which needs to be sent to minecraft.
     *
     * @return Favicon String
     */
    public String getIcon()
    {
        return favicon;
    }

}
