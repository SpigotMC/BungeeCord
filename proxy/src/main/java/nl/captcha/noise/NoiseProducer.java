package nl.captcha.noise;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public interface NoiseProducer {
    public void makeNoise(BufferedImage image);
}
