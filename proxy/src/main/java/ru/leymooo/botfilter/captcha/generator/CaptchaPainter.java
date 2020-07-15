package ru.leymooo.botfilter.captcha.generator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Random;

public class CaptchaPainter
{

    private final int width = 128;
    private final int height = 128;
    private final Color background = Color.WHITE;
    private final Random rnd = new Random();

    public BufferedImage draw(Font font, Color fGround, String text)
    {
        if ( font == null )
        {
            throw new IllegalArgumentException( "Font can not be null." );
        }
        if ( fGround == null )
        {
            throw new IllegalArgumentException(
                    "Foreground color can not be null." );
        }
        if ( text == null || text.length() < 1 )
        {
            throw new IllegalArgumentException( "No text given." );
        }

        BufferedImage img = createImage();

        final Graphics g = img.getGraphics();
        try
        {
            final Graphics2D g2 = configureGraphics( g, font, fGround );

            draw( g2, text );
        } finally
        {
            g.dispose();
        }

        img = postProcess( img );

        return img;
    }

    protected BufferedImage createImage()
    {
        return new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
    }

    protected Graphics2D configureGraphics(Graphics g, Font font, Color fGround)
    {
        if ( !( g instanceof Graphics2D ) )
        {
            throw new IllegalStateException( "Graphics (" + g
                    + ") that is not an instance of Graphics2D." );
        }
        final Graphics2D g2 = (Graphics2D) g;

        configureGraphicsQuality( g2 );

        g2.setColor( fGround );
        g2.setBackground( background );
        g2.setFont( font );

        g2.clearRect( 0, 0, width, height );

        return g2;
    }

    protected void configureGraphicsQuality(Graphics2D g2)
    {

        g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        g2.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON );
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON );
        g2.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY );
        g2.setRenderingHint( RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE );
        g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        g2.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
        g2.setRenderingHint( RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY );

    }

    protected void draw(Graphics2D g, String text)
    {
        final GlyphVector vector = g.getFont().createGlyphVector(
                g.getFontRenderContext(), text );

        transform( g, text, vector );

        final Rectangle bounds = vector.getPixelBounds( null, 0, height );
        final float bw = (float) bounds.getWidth();
        final float bh = (float) bounds.getHeight();

        final boolean outlineEnabled = true;

        final float wr = width / bw
                * ( rnd.nextFloat() / 20 + ( outlineEnabled ? 0.89f : 0.92f ) )
                * 1;
        final float hr = height / bh
                * ( rnd.nextFloat() / 20 + ( outlineEnabled ? 0.68f : 0.75f ) )
                * 1;
        g.translate( ( width - bw * wr ) / 2, ( height - bh * hr ) / 2 );
        g.scale( wr, hr );

        final float bx = (float) bounds.getX();
        final float by = (float) bounds.getY();
        if ( outlineEnabled )
        {
            g.draw( vector.getOutline( Math.signum( rnd.nextFloat() - 0.5f ) * 1
                    * width / 200 - bx, Math.signum( rnd.nextFloat() - 0.5f ) * 1
                    * height / 70 + height - by ) );
        }
        g.drawGlyphVector( vector, -bx, height - by );
    }

    protected void transform(Graphics2D g, String text, GlyphVector v)
    {
        final int glyphNum = v.getNumGlyphs();

        Point2D prePos = null;
        Rectangle2D preBounds = null;

        double rotateCur = ( rnd.nextDouble() - 0.5 ) * Math.PI / 8;
        double rotateStep = Math.signum( rotateCur )
                * ( rnd.nextDouble() * 3 * Math.PI / 8 / glyphNum );
        final boolean rotateEnabled = true;

        for ( int fi = 0; fi < glyphNum; fi++ )
        {
            if ( rotateEnabled )
            {
                final AffineTransform tr = AffineTransform
                        .getRotateInstance( rotateCur );
                if ( rnd.nextDouble() < 0.25 )
                {
                    rotateStep *= -1;
                }
                rotateCur += rotateStep;
                v.setGlyphTransform( fi, tr );
            }
            final Point2D pos = v.getGlyphPosition( fi );
            final Rectangle2D bounds = v.getGlyphVisualBounds( fi ).getBounds2D();
            Point2D newPos;
            if ( prePos == null )
            {
                newPos = new Point2D.Double( pos.getX() - bounds.getX(),
                        pos.getY() );
            } else
            {
                newPos = new Point2D.Double(
                        preBounds.getMaxX()
                        + pos.getX()
                        - bounds.getX()
                        - Math.min( preBounds.getWidth(),
                                bounds.getWidth() )
                        * ( rnd.nextDouble() / 20 + ( rotateEnabled ? 0.27
                        : 0.1 ) ), pos.getY() );
            }
            v.setGlyphPosition( fi, newPos );
            prePos = newPos;
            preBounds = v.getGlyphVisualBounds( fi ).getBounds2D();
        }
    }

    protected BufferedImage postProcess(BufferedImage img)
    {
        if ( /*effectConfig.isRippleEnabled()*/ true )
        {
            final Rippler.AxisConfig vertical = new Rippler.AxisConfig(
                    rnd.nextDouble() * 2 * Math.PI, ( 1 + rnd.nextDouble() * 2 )
                    * Math.PI, img.getHeight() / 10.0 );
            final Rippler.AxisConfig horizontal = new Rippler.AxisConfig(
                    rnd.nextDouble() * 2 * Math.PI, ( 2 + rnd.nextDouble() * 2 )
                    * Math.PI, img.getWidth() / 100.0 );
            final Rippler op = new Rippler( vertical, horizontal );

            img = op.filter( img, createImage() );
        }
        if ( /*effectConfig.isBlurEnabled()*/ true )
        {
            final float[] blurArray = new float[ 9 ];
            fillBlurArray( blurArray );
            final ConvolveOp op = new ConvolveOp( new Kernel( 3, 3, blurArray ),
                    ConvolveOp.EDGE_NO_OP, null );

            img = op.filter( img, createImage() );
        }
        return img;
    }

    protected void fillBlurArray(float[] array)
    {
        float sum = 0;
        for ( int fi = 0; fi < array.length; fi++ )
        {
            array[fi] = rnd.nextFloat();
            sum += array[fi];
        }
        for ( int fi = 0; fi < array.length; fi++ )
        {
            array[fi] /= sum;
        }
    }
}
