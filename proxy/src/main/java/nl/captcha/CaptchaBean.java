package nl.captcha;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import nl.captcha.backgrounds.BackgroundProducer;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.gimpy.GimpyRenderer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;
import nl.captcha.text.renderer.WordRenderer;


/**
 * Simple CAPTCHA bean intended to be used by Spring.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James
 *
 */
public class CaptchaBean {
	private BackgroundProducer _bgProd = new GradiatedBackgroundProducer();
	private TextProducer _txtProd;
	private NoiseProducer _noiseProd;
	private GimpyRenderer _gimpy;
	private boolean _addBorder = false;
	
	private String _answer = "";
	private BufferedImage _img;
	private BufferedImage _bg;
	
	public CaptchaBean(int width, int height) {
		_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	public CaptchaBean build() {
		// Add and render the text
		if (_txtProd != null) {
			_answer += _txtProd.getText();
			WordRenderer wr = new DefaultWordRenderer();
			wr.render(_answer, _img);
		}
		
		if (_noiseProd != null) {
			_noiseProd.makeNoise(_img);
		}
		
		if (_gimpy != null) {
			_gimpy.gimp(_img);
		}
		
		_bg = _bgProd.getBackground(_img.getWidth(), _img.getHeight());
		
    	// Paint the main image over the background
    	Graphics2D g = _bg.createGraphics();
    	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    	g.drawImage(_img, null, null);
    	
    	// Add the border, if necessary
    	if (_addBorder) {
    		int width = _img.getWidth();
    		int height = _img.getHeight();
    		
            g.setColor(Color.BLACK);
            g.drawLine(0, 0, 0, width);
            g.drawLine(0, 0, width, 0);
            g.drawLine(0, height - 1, width, height - 1);
            g.drawLine(width - 1, height - 1, width - 1, 0);
    	}
    	_img = _bg;
    	g.dispose();
		
		return this;
	}
	
	public boolean isCorrect(String answer) {
		return answer.equals(_answer);
	}
	
	public BufferedImage getImage() {
		return _img;
	}
	
	public BackgroundProducer getBgProd() {
		return _bgProd;
	}
	public void setBgProd(BackgroundProducer bgProd) {
		_bgProd = bgProd;
	}
	public TextProducer getTxtProd() {
		return _txtProd;
	}
	public void setTxtProd(TextProducer txtProd) {
		_txtProd = txtProd;
	}
	public NoiseProducer getNoiseProd() {
		return _noiseProd;
	}
	public void setNoiseProd(NoiseProducer noiseProd) {
		_noiseProd = noiseProd;
	}
	public GimpyRenderer getGimpy() {
		return _gimpy;
	}
	public void setGimpy(GimpyRenderer gimpy) {
		_gimpy = gimpy;
	}
	public boolean isAddBorder() {
		return _addBorder;
	}
	public void setAddBorder(boolean addBorder) {
		_addBorder = addBorder;
	}
	public String getAnswer() {
		return _answer;
	}
	public void setAnswer(String answer) {
		_answer = answer;
	}
}
