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
package com.github.cage;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.github.cage.image.Painter;
import com.github.cage.image.RgbColorGenerator;
import com.github.cage.token.RandomTokenGenerator;

/**
 * Convenient entry class to control captcha generation. This class is thread
 * safe. Example usage:
 * 
 * <p>
 * 
 * <pre>
 * <code>
 * Cage cage = new {@link GCage}(); // lets make some "G" template captchas
 * // use new {@link YCage}() for "Y" template or configure it yourself with
 * // one of the Cage {@link #Cage(Painter, IGenerator, IGenerator, String,
 * Float, IGenerator, Random) constructors}.
 * 
 * // ...
 * 
 * String token1 = cage.{@link #getTokenGenerator() getTokenGenerator()}.next();
 * String token2 = cage.{@link #getTokenGenerator() getTokenGenerator()}.next();
 * 
 * cage.{@link #draw(String, OutputStream) draw}(token1, someOutputstream1);
 * cage.{@link #draw(String, OutputStream) draw}(token2, someOutputstream2);
 * 
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author akiraly
 * 
 */
public class Cage {
	/**
	 * Default compress ratio for image encoders.
	 */
	public static final Float DEFAULT_COMPRESS_RATIO = 0.5f;

	/**
	 * Default image encoding format.
	 */
	public static final String DEFAULT_FORMAT = "jpeg";

	private final Painter painter;
	private final IGenerator<Font> fonts;
	private final IGenerator<Color> foregrounds;
	private final String format;
	private final Float compressRatio;
	private final IGenerator<String> tokenGenerator;

	/**
	 * Default constructor. Calls
	 * {@link Cage#Cage(Painter, IGenerator, IGenerator, String, Float, IGenerator, Random)}
	 */
	public Cage() {
		this(null, null, null, null, DEFAULT_COMPRESS_RATIO, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param painter
	 *            to be used for painting, can be null
	 * @param fonts
	 *            generator used to generate fonts for texts, defaults to a
	 *            random chooser from some predefined set of fonts, can be null
	 * @param foregrounds
	 *            generator used to generate colors for texts, defaults to a
	 *            random "not-bright-so-it-is-readable-on-white" color
	 *            generator, can be null
	 * @param format
	 *            output format, default "jpeg", can be null
	 * @param compressRatio
	 *            a number in [0f, 1f] interval if compression should be used
	 *            with the output format. The format must support compression
	 *            (like jpeg and png). If null no compression is done.
	 * @param tokenGenerator
	 *            a custom String token generator, can be null. If null is
	 *            passed a default is created. It is not used by Cage it is only
	 *            stored for convenience. Can be retrieved by
	 *            {@link #getTokenGenerator()}.
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public Cage(Painter painter, IGenerator<Font> fonts,
			IGenerator<Color> foregrounds, String format, Float compressRatio,
			IGenerator<String> tokenGenerator, Random rnd) {
		if (rnd == null) {
			rnd = new Random();
		}
		this.painter = painter != null ? painter : new Painter(rnd);
		int defFontHeight = this.painter.getHeight() / 2;
		this.fonts = fonts != null ? fonts : new ObjectRoulette<Font>(rnd, //
				new Font(Font.SANS_SERIF, Font.PLAIN, defFontHeight), //
				// new Font(Font.SANS_SERIF, Font.ITALIC, defFontHeight),//
				new Font(Font.SERIF, Font.PLAIN, defFontHeight), //
				// new Font(Font.SERIF, Font.ITALIC, defFontHeight), //
				new Font(Font.MONOSPACED, Font.BOLD, defFontHeight)); //
		// new Font(Font.MONOSPACED, Font.ITALIC, defFontHeight));
		this.foregrounds = foregrounds != null ? foregrounds
				: new RgbColorGenerator(rnd);
		this.format = format != null ? format : DEFAULT_FORMAT;
		this.compressRatio = compressRatio;
		this.tokenGenerator = tokenGenerator != null ? tokenGenerator
				: new RandomTokenGenerator(rnd);
	}

	/**
	 * Generate an image and serialize it to the output. This method can call
	 * {@link OutputStream#close()} on the supplied output stream.
	 * 
	 * @param text
	 *            to be drawn on the image
	 * @param ostream
	 *            captcha image is serialized to this
	 * @throws IOException
	 *             if IO error occurs.
	 */
	public void draw(String text, OutputStream ostream) throws IOException {
		BufferedImage img = drawImage(text);
		serialize(img, ostream);
	}

	/**
	 * Generate an image and return it in a byte array.
	 * 
	 * @param text
	 *            to be drawn on the image
	 * @return byte array holding the serialized generated image
	 */
	public byte[] draw(String text) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			try {
				draw(text, baos);
			} finally {
				baos.close();
			}
		} catch (IOException e) {
			// IO errors should not happen we were writing to memory
			throw new RuntimeException(
					"IO error while writing captcha image to memory.", e);
		}

		return baos.toByteArray();
	}

	/**
	 * Generates a captcha image.
	 * 
	 * @param text
	 *            to be drawn
	 * @return generated image
	 */
	public BufferedImage drawImage(String text) {
		Font font = fonts.next();
		Color fground = foregrounds.next();
		return painter.draw(font, fground, text);
	}

	/**
	 * Serializes an image to an {@link OutputStream}. This method can call
	 * {@link OutputStream#close()} on the supplied output stream.
	 * 
	 * @param img
	 *            to be serialized
	 * @param ostream
	 *            to be written to
	 * @throws IOException
	 *             if IO error occurs.
	 */
	protected void serialize(BufferedImage img, OutputStream ostream)
			throws IOException {
		ImageTypeSpecifier type = ImageTypeSpecifier
				.createFromRenderedImage(img);
		Iterator<ImageWriter> iwi = ImageIO.getImageWriters(type, this.format);
		if (iwi == null || !iwi.hasNext()) {
			throw new IllegalStateException(
					"No image writer found for format = " + this.format);
		}
		ImageWriter iw = iwi.next();
		try {
			ImageWriteParam iwp;
			if (compressRatio != null) {
				iwp = iw.getDefaultWriteParam();
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				iwp.setCompressionQuality(compressRatio);
			} else {
				iwp = null;
			}
			ImageOutputStream ios = ImageIO.createImageOutputStream(ostream);
			try {
				iw.setOutput(ios);
				iw.write(null, new IIOImage(img, null, null), iwp);
			} finally {
				ios.close();
			}
		} finally {
			iw.dispose();
		}
	}

	/**
	 * @return object used to draw the image, not null
	 */
	public Painter getPainter() {
		return painter;
	}

	/**
	 * @return font generator used to choose a font, not null
	 */
	public IGenerator<Font> getFonts() {
		return fonts;
	}

	/**
	 * @return foreground generator used to choose a text color, not null
	 */
	public IGenerator<Color> getForegrounds() {
		return foregrounds;
	}

	/**
	 * @return used image encoding format, like "jpeg", not null
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return compress ratio used by image encoding, can be null
	 */
	public Float getCompressRatio() {
		return compressRatio;
	}

	/**
	 * @return token generator to produce strings for the image, not null
	 */
	public IGenerator<String> getTokenGenerator() {
		return tokenGenerator;
	}
}
