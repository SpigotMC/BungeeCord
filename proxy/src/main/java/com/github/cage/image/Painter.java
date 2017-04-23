/*
 * Copyright 2011 Király Attila
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cage.image;

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

/**
 * This class does most of the captcha drawing. This class is thread safe.
 * 
 * @author akiraly
 * 
 */
public class Painter {
	/**
	 * Enumeration for different image quality levels.
	 */
	public static enum Quality {
		/**
		 * Rendering hints should be set to minimum quality.
		 */
		MIN, /**
		 * Rendering hints should be not set so they use the default.
		 * quality
		 */
		DEFAULT, /**
		 * Rendering hints should be set to maximum quality.
		 */
		MAX
	}

	/**
	 * Default image width.
	 */
	public static final int DEFAULT_WIDTH = 200;

	/**
	 * Default image height.
	 */
	public static final int DEFAULT_HEIGHT = 70;

	private final int width;
	private final int height;
	private final Color background;
	private final Quality quality;
	private final EffectConfig effectConfig;
	private final Random rnd;

	/**
	 * Constructor.
	 */
	public Painter() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, null, null, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public Painter(Random rnd) {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT, null, null, null, rnd);
	}

	/**
	 * Constructor.
	 * 
	 * @param width
	 *            captcha image width, default {@link #DEFAULT_WIDTH}
	 * @param height
	 *            captcha image height, default {@link #DEFAULT_HEIGHT}
	 * @param bGround
	 *            background color of captcha image, default white, can be null
	 * @param quality
	 *            captcha image quality, default {@link Quality#MAX}, should use
	 *            max it does not have measurable speed penalty on modern jvm-s
	 *            (1.6u23), can be null
	 * @param effectConfig
	 *            used to define what effects should be used, can be null
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public Painter(int width, int height, Color bGround, Quality quality,
			EffectConfig effectConfig, Random rnd) {
		super();
		this.width = width;
		this.height = height;
		this.background = bGround != null ? bGround : Color.WHITE;
		this.quality = quality != null ? quality : Quality.MAX;
		this.effectConfig = effectConfig != null ? effectConfig
				: new EffectConfig();
		this.rnd = rnd != null ? rnd : new Random();
	}

	/**
	 * Generates a new captcha image.
	 * 
	 * @param font
	 *            will be used for text, not null
	 * @param fGround
	 *            will be used for text, not null
	 * @param text
	 *            this will be rendered on the image, not null, not 0 length
	 * @return the generated image
	 */
	public BufferedImage draw(Font font, Color fGround, String text) {
		if (font == null) {
			throw new IllegalArgumentException("Font can not be null.");
		}
		if (fGround == null) {
			throw new IllegalArgumentException(
					"Foreground color can not be null.");
		}
		if (text == null || text.length() < 1) {
			throw new IllegalArgumentException("No text given.");
		}

		BufferedImage img = createImage();

		Graphics g = img.getGraphics();
		try {
			Graphics2D g2 = configureGraphics(g, font, fGround);

			draw(g2, text);
		} finally {
			g.dispose();
		}

		img = postProcess(img);

		return img;
	}

	/**
	 * Creates a new image to draw upon.
	 * 
	 * @return new image, not null
	 */
	protected BufferedImage createImage() {
		return new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}

	/**
	 * Configures graphics object before drawing text on it.
	 * 
	 * @param g
	 *            to be configured must be an instance of {@link Graphics2D},
	 *            not null
	 * @param font
	 *            to be used for the text, not null
	 * @param fGround
	 *            to be used for the text, not null
	 * 
	 * @return g casted to {@link Graphics2D} or throws exception if g is not
	 *         instance of {@link Graphics2D}.
	 */
	protected Graphics2D configureGraphics(Graphics g, Font font, Color fGround) {
		if (!(g instanceof Graphics2D)) {
			throw new IllegalStateException("Graphics (" + g
					+ ") that is not an instance of Graphics2D.");
		}
		Graphics2D g2 = (Graphics2D) g;

		configureGraphicsQuality(g2);

		g2.setColor(fGround);
		g2.setBackground(background);
		g2.setFont(font);

		g2.clearRect(0, 0, width, height);

		return g2;
	}

	/**
	 * Sets quality related hints based on the quality field of this object.
	 * 
	 * @param g2
	 *            to be configured, not null
	 */
	protected void configureGraphicsQuality(Graphics2D g2) {
		if (quality == Quality.MAX) {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_ENABLE);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
		} else if (quality == Quality.MIN) {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
					RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_DISABLE);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED);
		}
	}

	/**
	 * Does some of the text transformation (calls
	 * {@link #transform(Graphics2D, String, GlyphVector)}), scales, transforms
	 * and draws the result (also the outline if needed).
	 * 
	 * @param g
	 *            to be drawn upon
	 * @param text
	 *            to be drawn
	 */
	protected void draw(Graphics2D g, String text) {
		GlyphVector vector = g.getFont().createGlyphVector(
				g.getFontRenderContext(), text);

		transform(g, text, vector);

		Rectangle bounds = vector.getPixelBounds(null, 0, height);
		float bw = (float) bounds.getWidth();
		float bh = (float) bounds.getHeight();

		boolean outlineEnabled = effectConfig.isOutlineEnabled();

		// transform + scale text to better fit the image
		float wr = width / bw
				* (rnd.nextFloat() / 20 + (outlineEnabled ? 0.89f : 0.92f))
				* effectConfig.getScaleConfig().getX();
		float hr = height / bh
				* (rnd.nextFloat() / 20 + (outlineEnabled ? 0.68f : 0.75f))
				* effectConfig.getScaleConfig().getY();
		g.translate((width - bw * wr) / 2, (height - bh * hr) / 2);
		g.scale(wr, hr);

		float bx = (float) bounds.getX();
		float by = (float) bounds.getY();
		// draw outline if needed
		if (outlineEnabled) {
			g.draw(vector.getOutline(Math.signum(rnd.nextFloat() - 0.5f) * 1
					* width / 200 - bx, Math.signum(rnd.nextFloat() - 0.5f) * 1
					* height / 70 + height - by));
		}
		g.drawGlyphVector(vector, -bx, height - by);
	}

	/**
	 * Does some of the text transformation (like rotation and symbol crowding).
	 * 
	 * @param g
	 *            to be drawn upon
	 * @param text
	 *            to be drawn
	 * @param v
	 *            graphical representation of text, to be transformed
	 */
	protected void transform(Graphics2D g, String text, GlyphVector v) {
		int glyphNum = v.getNumGlyphs();

		Point2D prePos = null;
		Rectangle2D preBounds = null;

		double rotateCur = (rnd.nextDouble() - 0.5) * Math.PI / 8;
		double rotateStep = Math.signum(rotateCur)
				* (rnd.nextDouble() * 3 * Math.PI / 8 / glyphNum);
		boolean rotateEnabled = effectConfig.isRotateEnabled();

		for (int fi = 0; fi < glyphNum; fi++) {
			if (rotateEnabled) {
				AffineTransform tr = AffineTransform
						.getRotateInstance(rotateCur);
				if (rnd.nextDouble() < 0.25) {
					rotateStep *= -1;
				}
				rotateCur += rotateStep;
				v.setGlyphTransform(fi, tr);
			}
			Point2D pos = v.getGlyphPosition(fi);
			Rectangle2D bounds = v.getGlyphVisualBounds(fi).getBounds2D();
			Point2D newPos;
			if (prePos == null) {
				newPos = new Point2D.Double(pos.getX() - bounds.getX(),
						pos.getY());
			} else {
				newPos = new Point2D.Double(
						preBounds.getMaxX()
								+ pos.getX()
								- bounds.getX()
								- Math.min(preBounds.getWidth(),
										bounds.getWidth())
								* (rnd.nextDouble() / 20 + (rotateEnabled ? 0.27
										: 0.1)), pos.getY());
			}
			v.setGlyphPosition(fi, newPos);
			prePos = newPos;
			preBounds = v.getGlyphVisualBounds(fi).getBounds2D();
		}
	}

	/**
	 * Does some post processing on the generated image if needed. Like rippling
	 * (waving) and blurring.
	 * 
	 * @param img
	 *            to be post prosessed.
	 * @return the finished image, maybe the same as the input
	 */
	protected BufferedImage postProcess(BufferedImage img) {
		if (effectConfig.isRippleEnabled()) {
			Rippler.AxisConfig vertical = new Rippler.AxisConfig(
					rnd.nextDouble() * 2 * Math.PI, (1 + rnd.nextDouble() * 2)
							* Math.PI, img.getHeight() / 10.0);
			Rippler.AxisConfig horizontal = new Rippler.AxisConfig(
					rnd.nextDouble() * 2 * Math.PI, (2 + rnd.nextDouble() * 2)
							* Math.PI, img.getWidth() / 100.0);
			Rippler op = new Rippler(vertical, horizontal);

			img = op.filter(img, createImage());
		}
		if (effectConfig.isBlurEnabled()) {
			float[] blurArray = new float[9];
			fillBlurArray(blurArray);
			ConvolveOp op = new ConvolveOp(new Kernel(3, 3, blurArray),
					ConvolveOp.EDGE_NO_OP, null);

			img = op.filter(img, createImage());
		}
		return img;
	}

	/**
	 * Generates a random probability distribution. Used by blurring.
	 * 
	 * @param array
	 *            filled with random values. The values in array sum up to 1.
	 */
	protected void fillBlurArray(float[] array) {
		float sum = 0;
		for (int fi = 0; fi < array.length; fi++) {
			array[fi] = rnd.nextFloat();
			sum += array[fi];
		}
		for (int fi = 0; fi < array.length; fi++) {
			array[fi] /= sum;
		}
	}

	/**
	 * @return width of the image
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return height of the image
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return background color of the image, not null
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * @return quality level of the image, not null
	 */
	public Quality getQuality() {
		return quality;
	}

	/**
	 * @return configuration for effects, not null
	 */
	public EffectConfig getEffectConfig() {
		return effectConfig;
	}
}
