package net.md_5.bungee.api;

import com.google.common.io.BaseEncoding;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.imageio.ImageIO;

/**
 * @author TheJeterLP
 */
public class Favicon
{

    private String favicon;

    public Favicon(File fav)
    {
        if ( fav == null )
        {
            return;
        }
        try
        {
            if ( !fav.exists() )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon because it does not exist!" );
                return;
            }
            if ( !fav.getName().endsWith( ".png" ) )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon because it is not a PNG file!" );
                return;
            }
            if ( !fav.isFile() )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon because it is not a file!" );
                return;
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
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "Favicon file too large for server to process" );
                        favicon = null;
                    }
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Server icon must be exactly 64x64 pixels" );
                }
            } else
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon for unknown reason. Please double check its format." );
            }
        } catch ( IOException e )
        {
            favicon = "";
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
