package nl.captcha.gimpy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Overlays a warped grid to the image.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class FishEyeGimpyRenderer implements GimpyRenderer {
	private final Color _hColor;
	private final Color _vColor;
	
	public FishEyeGimpyRenderer() {
		this(Color.BLUE, Color.BLUE);
	}
	
	public FishEyeGimpyRenderer(Color hColor, Color vColor) {
		_hColor = hColor;
		_vColor = vColor;
	}

	@Override
    public void gimp(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        int hstripes = height / 7;
        int vstripes = width / 7;

        // Calculate space between lines
        int hspace = height / (hstripes + 1);
        int vspace = width / (vstripes + 1);

        Graphics2D graph = (Graphics2D) image.getGraphics();
        // Draw the horizontal stripes
        for (int i = hspace; i < height; i = i + hspace) {
            graph.setColor(_hColor);
            graph.drawLine(0, i, width, i);
        }

        // Draw the vertical stripes
        for (int i = vspace; i < width; i = i + vspace) {
            graph.setColor(_vColor);
            graph.drawLine(i, 0, i, height);
        }

        // Create a pixel array of the original image.
        // we need this later to do the operations on..
        int pix[] = new int[height * width];
        int j = 0;

        for (int j1 = 0; j1 < width; j1++) {
            for (int k1 = 0; k1 < height; k1++) {
                pix[j] = image.getRGB(j1, k1);
                j++;
            }
        }


        graph.dispose();
    }


}
