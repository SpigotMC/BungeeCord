package nl.captcha.backgrounds;

import java.awt.image.BufferedImage;

/**
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public interface BackgroundProducer {

    /**
     * Add the background to the given image.
     * 
     * @param image The image onto which the background will be rendered.
     * @return The image with the background rendered.
     */
    public BufferedImage addBackground(BufferedImage image);
    
    public BufferedImage getBackground(int width, int height);
}
