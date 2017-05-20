package nl.captcha.text.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders the answer onto the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 */
public class DefaultWordRenderer implements WordRenderer {

    private static final Random RAND = new SecureRandom();
    private static final List<Color> DEFAULT_COLORS = new ArrayList<Color>();
    private static final List<Font> DEFAULT_FONTS = new ArrayList<Font>();
    // The text will be rendered 25%/5% of the image height/width from the X and Y axes
    private static final double YOFFSET = 0.35;
    private static final double XOFFSET = 0.01;

    static {
        DEFAULT_COLORS.add(Color.RED);
        DEFAULT_COLORS.add(Color.yellow);
        DEFAULT_COLORS.add(Color.CYAN);
        DEFAULT_COLORS.add(Color.WHITE);
        DEFAULT_COLORS.add(Color.PINK);
        DEFAULT_COLORS.add(Color.BLACK);
        DEFAULT_FONTS.add(new Font(Font.DIALOG, Font.BOLD, (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) == true ? 65:56));
    }

    private final List<Color> _colors = new ArrayList<Color>();
    private final List<Font> _fonts = new ArrayList<Font>();

    /**
     * Use the default color (black) and fonts (Arial and Courier).
     */
    public DefaultWordRenderer() {
        this(DEFAULT_COLORS, DEFAULT_FONTS);
    }

    /**
     * Build a <code>WordRenderer</code> using the given <code>Color</code>s and
     * <code>Font</code>s.
     * 
     * @param colors
     * @param fonts
     */
    public DefaultWordRenderer(List<Color> colors, List<Font> fonts) {
        _colors.addAll(colors);
        _fonts.addAll(fonts);
    }

    /**
     * Render a word onto a BufferedImage.
     * 
     * @param word The word to be rendered.
     * @param image The BufferedImage onto which the word will be painted.
     */
    @Override
    public void render(final String word, BufferedImage image) {
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontRenderContext frc = g.getFontRenderContext();
        int xBaseline = (int) Math.round(image.getWidth() * XOFFSET);
        int yBaseline =  image.getHeight() - (int) Math.round(image.getHeight() * YOFFSET);

        char[] chars = new char[1];
        for (char c : word.toCharArray()) {
            chars[0] = c;

            g.setColor(_colors.get(RAND.nextInt(_colors.size())));

            int choiceFont = RAND.nextInt(_fonts.size());
            Font font = _fonts.get(choiceFont);
            g.setFont(font);
            GlyphVector gv = font.createGlyphVector(frc, chars);
            g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

            int width = (int) gv.getVisualBounds().getWidth();
            xBaseline = xBaseline + width;
        }
    }
}
