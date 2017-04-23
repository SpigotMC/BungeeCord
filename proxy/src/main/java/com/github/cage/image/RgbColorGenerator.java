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
import java.util.Random;

import com.github.cage.IGenerator;

/**
 * Random RGB {@link Color} object generator. The returned {@link Color}-s are
 * not too bright so they look good on white background. This class is thread
 * safe.
 * 
 * @author akiraly
 */
public class RgbColorGenerator implements IGenerator<Color> {
	private final Random rnd;

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public RgbColorGenerator(Random rnd) {
		this.rnd = rnd != null ? rnd : new Random();
	}

	public Color next() {
		int[] c = new int[3];

		int i = rnd.nextInt(c.length);

		for (int fi = 0; fi < c.length; fi++) {
			if (fi == i) {
				c[fi] = rnd.nextInt(71);
			} else {
				c[fi] = rnd.nextInt(256);
			}
		}

		return new Color(c[0], c[1], c[2]);
	}

}
