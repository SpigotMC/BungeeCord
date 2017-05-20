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

import ru.leymooo.captcha.Configuration;

/**
 * Class to represent used effect configuration by {@link Painter}.
 * 
 * @author akiraly
 */
public class EffectConfig {
	private final boolean rippleEnabled;
	private final boolean blurEnabled;
	private final boolean outlineEnabled;
	private final boolean rotateEnabled;
	private final ScaleConfig scaleConfig;

	/**
	 * Constructor.
	 */
	public EffectConfig() {
		this(Configuration.getInstance().isRipple(), Configuration.getInstance().isBlur(), Configuration.getInstance().isOutline(), Configuration.getInstance().isRotate(), null);
	}

	/**
	 * Constructor.
	 * 
	 * @param rippleEnabled
	 *            waving effect should be used, default true, disabling this
	 *            helps performance
	 * @param blurEnabled
	 *            should the image be blurred, default true, disabling this
	 *            helps performance
	 * @param outlineEnabled
	 *            should a shifted, font colored outline be drawn behind the
	 *            characters, default false, disabling this helps performance
	 *            slightly
	 * @param rotateEnabled
	 *            should the letters be rotated independently, default true,
	 *            disabling this helps performance slightly
	 * @param scaleConfig
	 *            scaling information for the captcha image, can be null
	 */
	public EffectConfig(boolean rippleEnabled, boolean blurEnabled,
			boolean outlineEnabled, boolean rotateEnabled,
			ScaleConfig scaleConfig) {
		super();
		this.rippleEnabled = rippleEnabled;
		this.blurEnabled = blurEnabled;
		this.outlineEnabled = outlineEnabled;
		this.rotateEnabled = rotateEnabled;
		this.scaleConfig = scaleConfig != null ? scaleConfig
				: new ScaleConfig();
	}

	/**
	 * @return true if the image will be rippled (waved)
	 */
	public boolean isRippleEnabled() {
		return rippleEnabled;
	}

	/**
	 * @return true if the image will be blurred
	 */
	public boolean isBlurEnabled() {
		return blurEnabled;
	}

	/**
	 * @return true if outline shadow for text will be drawn on the image
	 */
	public boolean isOutlineEnabled() {
		return outlineEnabled;
	}

	/**
	 * @return true if the text letters will be rotated before drawn on the
	 *         image
	 */
	public boolean isRotateEnabled() {
		return rotateEnabled;
	}

	/**
	 * @return scaling information for the captcha image, not null
	 */
	public ScaleConfig getScaleConfig() {
		return scaleConfig;
	}
}
