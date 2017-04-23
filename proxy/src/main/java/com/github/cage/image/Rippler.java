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

import java.awt.image.BufferedImage;

/**
 * A filter to generate ripple (wave) effected images. Uses a transformed sinus
 * wave for this. This class is thread safe.
 * 
 * @author akiraly
 * 
 */
public class Rippler {
	/**
	 * Class to respresent wave tranforming information for an axis.
	 */
	public static class AxisConfig {
		private final double start;

		private final double length;

		private final double amplitude;

		/**
		 * Constructor.
		 * 
		 * @param start
		 *            the starting x offset to generate wave values. Should be
		 *            between 0 and 2 * {@link Math#PI}.
		 * @param length
		 *            the length of x to be used to generate wave values. Should
		 *            be between 0 and 4 * {@link Math#PI}.
		 * @param amplitude
		 *            the maximum y value, if it is too big, some important
		 *            parts of the image (like the text) can "wave" out on the
		 *            top or on the bottom of the image.
		 */
		public AxisConfig(double start, double length, double amplitude) {
			this.start = normalize(start, 2);
			this.length = normalize(length, 4);
			this.amplitude = amplitude;
		}

		/**
		 * Normalizes parameter to fall into [0, multi * {@link Math#PI}].
		 * 
		 * @param a
		 *            to be normalized
		 * @param multi
		 *            multiplicator used for end value
		 * @return normalized value
		 */
		protected double normalize(double a, int multi) {
			double piMulti = multi * Math.PI;

			a = Math.abs(a);
			double d = Math.floor(a / piMulti);

			return a - d * piMulti;
		}

		/**
		 * @return wave part start value
		 */
		public double getStart() {
			return start;
		}

		/**
		 * @return wave part length
		 */
		public double getLength() {
			return length;
		}

		/**
		 * @return amplitude used to transform the wave part
		 */
		public double getAmplitude() {
			return amplitude;
		}
	}

	private final AxisConfig vertical;

	private final AxisConfig horizontal;

	/**
	 * Constructor.
	 * 
	 * @param vertical
	 *            config to calculate waving deltas from x axis (so to modify y
	 *            values), not null
	 * @param horizontal
	 *            config to calculate waving deltas from y axis (so to modify x
	 *            values), not null
	 */
	public Rippler(AxisConfig vertical, AxisConfig horizontal) {
		this.vertical = vertical;
		this.horizontal = horizontal;
	}

	/**
	 * Draws a rippled (waved) variant of source into destination.
	 * 
	 * @param src
	 *            to be transformed, not null
	 * @param dest
	 *            to hold the result, not null
	 * @return dest is returned
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		int[] verticalDelta = calcDeltaArray(vertical, width);

		int[] horizontalDelta = calcDeltaArray(horizontal, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int ny = (y + verticalDelta[x] + height) % height;
				int nx = (x + horizontalDelta[ny] + width) % width;
				dest.setRGB(nx, ny, src.getRGB(x, y));
			}
		}

		return dest;
	}

	/**
	 * Calculates wave delta array.
	 * 
	 * @param axisConfig
	 *            config object to transform the wave, not null
	 * @param num
	 *            number of points needed, positive
	 * @return the calculated num length delta array
	 */
	protected int[] calcDeltaArray(AxisConfig axisConfig, int num) {
		int[] delta = new int[num];

		double start = axisConfig.getStart();
		double period = axisConfig.getLength() / num;
		double amplitude = axisConfig.getAmplitude();

		for (int fi = 0; fi < num; fi++) {
			delta[fi] = (int) Math.round(amplitude
					* Math.sin(start + fi * period));
		}

		return delta;
	}

	/**
	 * @return vertical config, not null
	 */
	public AxisConfig getVertical() {
		return vertical;
	}

	/**
	 * @return horizontal config, not null
	 */
	public AxisConfig getHorizontal() {
		return horizontal;
	}
}
