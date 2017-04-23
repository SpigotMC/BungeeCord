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

/**
 * Used by {@link EffectConfig} to hold the scaling scalars for the captcha
 * image.
 * 
 * @author akiraly
 * 
 */
public class ScaleConfig {
	private final float x;
	private final float y;

	/**
	 * Constructor.
	 */
	public ScaleConfig() {
		this(1, 1);
	}

	/**
	 * Constructor.
	 * 
	 * @param x
	 *            a value in [0f, 1f]. 1 means the captcha will fill the whole
	 *            width of the picture.
	 * @param y
	 *            a value in [0f, 1f]. 1 means the captcha will fill the whole
	 *            height of the picture.
	 */
	public ScaleConfig(float x, float y) {
		super();
		this.x = Math.min(Math.abs(x), 1);
		this.y = Math.min(Math.abs(y), 1);
	}

	/**
	 * @return scale by the x axis
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return scale by the y axis
	 */
	public float getY() {
		return y;
	}
}
