package ru.leymooo.botfilter.captcha.generator.map;

import java.awt.Image;
import java.util.Arrays;
import ru.leymooo.botfilter.packets.MapDataPacket;


public class CraftMapCanvas
{

    private final byte[] buffer = new byte[ 16384 ];

    public CraftMapCanvas()
    {
        Arrays.fill( this.buffer, (byte) -1 );
    }

    public void setPixel(int x, int y, byte color)
    {
        if ( x >= 0 && y >= 0 && x < 128 && y < 128 )
        {
            if ( this.buffer[y * 128 + x] != color )
            {
                this.buffer[y * 128 + x] = color;
            }

        }
    }

    protected byte[] getBuffer()
    {
        return this.buffer;
    }

    @SuppressWarnings("deprecation")
    public void drawImage(int x, int y, Image image)
    {
        byte[] bytes = MapPalette.imageToBytes( image );

        for ( int x2 = 0; x2 < image.getWidth( null ); ++x2 )
        {
            for ( int y2 = 0; y2 < image.getHeight( null ); ++y2 )
            {
                this.setPixel( x + x2, y + y2, bytes[y2 * image.getWidth( null ) + x2] );
            }
        }

    }

    public MapDataPacket.MapDataNew getMapData()
    {
        byte[] buffer1 = new byte[ 16384 ];
        byte[] buf = this.getBuffer();

        for ( int i = 0; i < buf.length; ++i )
        {
            byte color = buf[i];

            if ( color >= 0 || color <= -113 )
            {
                buffer1[i] = color;
            }
        }

        return new MapDataPacket.MapDataNew( 128, 128, 0, 0, buffer1 );
    }
}
